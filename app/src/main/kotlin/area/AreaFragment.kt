package area

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import areas.AreaResultModel
import areas.AreasRepo
import elements.ElementsRepo
import icons.toIconResId
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import map.getErrorColor
import map.getOnSurfaceColor
import map.name
import map.toBoundingBox
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.btcmap.R
import org.btcmap.databinding.FragmentAreaBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AreaFragment : Fragment() {

    private val areasRepo: AreasRepo by inject()

    private val elementsRepo: ElementsRepo by inject()

    private val areaResultModel: AreaResultModel by sharedViewModel()

    private var _binding: FragmentAreaBinding? = null
    private val binding get() = _binding!!

    private val adapter = AreaAdapter(
        listener = object : AreaAdapter.Listener {
            override fun onMapClick() {
                viewLifecycleOwner.lifecycleScope.launch {
                    val area =
                        areasRepo.selectById(AreaFragmentArgs.fromBundle(requireArguments()).areaId)
                            ?: return@launch
                    areaResultModel.area.update { area }
                    findNavController().navigate(R.id.action_areaFragment_to_mapFragment)
                }
            }

            override fun onElementClick(item: AreaAdapter.Item.Element) {
                findNavController().navigate(
                    AreaFragmentDirections.actionAreaFragmentToElementFragment(
                        item.id,
                    ),
                )
            }

            override fun onUrlClick(url: HttpUrl) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url.toString())
                startActivity(intent)
            }
        },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { appBar, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            appBar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = insets.top
            }
            val navBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.list.setPadding(0, 0, 0, navBarsInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val area = runBlocking {
            areasRepo.selectById(AreaFragmentArgs.fromBundle(requireArguments()).areaId)
        } ?: return

        WindowCompat.getInsetsController(
            requireActivity().window,
            requireActivity().window.decorView,
        ).isAppearanceLightStatusBars =
            when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> true
                else -> false
            }

        binding.toolbar.title = area.name()

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val box = area.toBoundingBox()

                val elements = elementsRepo.selectElementIdIconAndTags(
                    minLat = box.latSouth,
                    maxLat = box.latNorth,
                    minLon = box.lonWest,
                    maxLon = box.lonEast,
                ).map {
                    val status: String
                    val statusColor: Int

                    val tags: JsonObject = Json.decodeFromString(it.tags ?: "{}")

                    if (tags["survey:date"]?.jsonPrimitive?.content.isNullOrBlank() && tags["check_date"]?.jsonPrimitive?.content.isNullOrBlank()) {
                        status = getString(R.string.outdated)
                        statusColor = requireContext().getErrorColor()
                    } else {
                        status = getString(R.string.up_to_date)
                        statusColor = requireContext().getOnSurfaceColor()
                    }

                    AreaAdapter.Item.Element(
                        id = it.id,
                        icon = AppCompatResources.getDrawable(
                            requireContext(), it.icon_id.toIconResId() ?: R.drawable.ic_place,
                        )!!,
                        name = tags["name"]?.jsonPrimitive?.content
                            ?: getString(R.string.unnamed_place),
                        status = status,
                        statusColor = statusColor,
                    )
                }.sortedBy { it.name }.toMutableList()

                val boundingBoxPaddingPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16f,
                    resources.displayMetrics,
                ).toInt()

                val map = AreaAdapter.Item.Map(
                    boundingBox = area.toBoundingBox(),
                    boundingBoxPaddingPx = boundingBoxPaddingPx,
                )

                val contact = AreaAdapter.Item.Contact(
                    website = area.tags["contact:website"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                    twitter = area.tags["contact:twitter"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                    telegram = area.tags["contact:telegram"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                    discord = area.tags["contact:discord"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                    youtube = area.tags["contact:youtube"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                )

                adapter.submitList(listOf(map, contact) + elements)
                binding.toolbar.subtitle = resources.getQuantityString(
                    R.plurals.d_places,
                    elements.size,
                    elements.size,
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}