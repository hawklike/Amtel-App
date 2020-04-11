package cz.prague.cvut.fit.steuejan.amtelapp.fragments

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

class MapFragment : AbstractBaseFragment(), OnMapReadyCallback
{
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap

    companion object
    {
        private const val ADDRESS = "address"

        fun newInstance(address: String = ""): MapFragment
        {
            val fragment = MapFragment()
            fragment.arguments = Bundle().apply {
                putString(ADDRESS, address)
            }
            return fragment
        }
    }

    override fun getName(): String = "MapFragment"

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
        map.clear()
        map.isMyLocationEnabled = false
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
        map = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        val sydney = LatLng(49.940434, 13.374128)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}