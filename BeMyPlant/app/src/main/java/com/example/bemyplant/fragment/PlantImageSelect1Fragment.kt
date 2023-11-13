package com.example.bemyplant.fragment

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.bemyplant.R
import com.example.bemyplant.databinding.FragmentPlantImageSelect1Binding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlantImageSelect1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlantImageSelect1Fragment : Fragment() {
    // TODO: Rename and change types of parameters
    val binding by lazy{ FragmentPlantImageSelect1Binding.inflate((layoutInflater))}
    //private lateinit var binding: FragmentPlantRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding.nextButton.setOnClickListener{
            findNavController().navigate(R.id.action_plantImageSelect1Fragment_to_plantImageSelect2Fragment)
        }
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlantImageSelect1Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlantImageSelect1Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}