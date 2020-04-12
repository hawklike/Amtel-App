package cz.prague.cvut.fit.steuejan.amtelapp.fragments.miscellaneous

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cz.prague.cvut.fit.steuejan.amtelapp.databinding.FragmentMapBinding
import cz.prague.cvut.fit.steuejan.amtelapp.fragments.abstracts.AbstractBaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : AbstractBaseFragment(), OnMapReadyCallback
{
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private var address: String? = null

    companion object
    {
        private const val ADDRESS = "address"

        fun newInstance(address: String?): MapFragment
        {
            val fragment = MapFragment()
            fragment.arguments = Bundle().apply {
                putString(ADDRESS, address)
            }
            return fragment
        }
    }

    override fun getName(): String = "MapFragment"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ADDRESS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            map.onCreate(savedInstanceState)
            map.getMapAsync(this@MapFragment)
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(::map.isInitialized)
        {
            map.clear()
            map.isMyLocationEnabled = false
        }
        binding.map.getMapAsync(null)
        binding.map.onDestroy()
        _binding = null
    }

    override fun onPause()
    {
        super.onPause()
        binding.map.onPause()
    }

    override fun onResume()
    {
        super.onResume()
        binding.map.onResume()
    }

    override fun onLowMemory()
    {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap)
    {
        val opava = LatLng(49.940659, 17.894798)
        map = googleMap

        launch {
            val address = withContext(Dispatchers.IO) {
                try
                {
                    address ?: return@withContext null
                    val foundAddresses = Geocoder(context).getFromLocationName(address,1)
                    if(foundAddresses.isNotEmpty()) foundAddresses[0]
                    else null
                }
                catch(ex: Exception) { null }
            }

            if(address == null) map.moveCamera(CameraUpdateFactory.newLatLngZoom(opava, 10f))
            else
            {
                val court = LatLng(address.latitude, address.longitude)
                map.addMarker(MarkerOptions().position(court).title("Zde hrají legendy"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(court, 15f))
            }
        }
    }
}