package com.example.istea.currentweather

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.istea.CityClass
import com.example.istea.R
import com.example.istea.convertImgIdToUri
import com.example.istea.databinding.FragmentCurrentWeatherBinding
import com.example.istea.formatTemperatureString
import com.example.istea.helper.WeatherDatabase
import kotlinx.android.synthetic.main.fragment_current_weather.*

class CurrentWeatherFragment : Fragment() {

    val cityClass: CityClass = CityClass()

    lateinit var binding: FragmentCurrentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_current_weather,
            container,
            false
        )

        val application = requireNotNull(this.activity).application

        val dataSource = WeatherDatabase.getInstance(application).weatherDao

        val viewModelFactory = CurrentWeatherViewModelFactory(dataSource, application)

        val currentWeatherViewModel =
            ViewModelProvider(this, viewModelFactory)
                .get(CurrentWeatherViewModel::class.java)

        binding.currentWeatherViewModel = currentWeatherViewModel

        binding.setLifecycleOwner(this)

        currentWeatherViewModel.navigateToRecentCities.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().navigate(
                    CurrentWeatherFragmentDirections
                        .actionCurrentWeatherFragmentToRecentCitiesFragment()
                )
                currentWeatherViewModel.doneNavigatingToRecentCities()
            }
        })

        currentWeatherViewModel.navigateToWeatherDetails.observe(viewLifecycleOwner, Observer {
            if (it) {
                var cityName = city_text.text.toString()
                currentWeatherViewModel.getWeatherDetail(cityName)
                findNavController().navigate(
                    CurrentWeatherFragmentDirections
                        .actionCurrentWeatherFragmentToWeatherDetailFragment(cityName)
                )
                currentWeatherViewModel.doneNavigatingToWeatherDetails()
            }
        })

        currentWeatherViewModel.recentCity.observe(viewLifecycleOwner, {
            it?.let {
                binding.moreDetailsOption.visibility = View.VISIBLE
                binding.cityText.text = it.cityName
                binding.weatherTempText.text = formatTemperatureString(it.temperature)
                binding.weatherDescText.text = it.weatherDescription
                val imgUri = convertImgIdToUri(it.appIconId)
                val imgView = binding.weatherImage
                Glide.with(imgView.context)
                    .load(imgUri)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.loading_animation)
                            .error(R.mipmap.unknown_weather)
                    )
                    .into(imgView)
            }
        })

        val noInternetText = "Comproba tu conexión a Internet y proba de nuevo."
        val wrongCityText = "No se pudo encontrar la ubicación ingresada."

        currentWeatherViewModel.noInternetStatus.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(context, noInternetText, Toast.LENGTH_LONG).show()
                currentWeatherViewModel.doneShowingNoInternetToast()
            }
        })

        currentWeatherViewModel.wrongCityEntered.observe(viewLifecycleOwner, {
            if (it) {
                Toast.makeText(context, wrongCityText, Toast.LENGTH_SHORT).show()
                currentWeatherViewModel.doneShowingWrongCityEnteredToast()
            }
        })

        binding.cityClass = cityClass

        binding.buttonCheckWeather.setOnClickListener { view: View ->

            updateCity(view)
        }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.display_mode_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateCity(view: View) {
        binding.apply {
            var cityNameInput = cityEditText.text.toString()
            if (cityNameInput != "") {
                currentWeatherViewModel!!.getWeatherDetail(cityNameInput.trim())
                cityEditText.text!!.clear()
                invalidateAll()
            }
        }
    }

}