package com.doobie.pdfrendercomparison.ui.scrolllist

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doobie.pdfrendercomparison.databinding.FragmentScrollListBinding
import com.doobie.pdfrendercomparison.getPdfFile
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScrollListFragment : Fragment() {

    lateinit var binding: FragmentScrollListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScrollListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {

            val file = getPdfFile(requireContext())

            binding.pdfView.fromFile(file)
                .pageSnap(true) // snap pages to screen boundaries
                .pageFling(false) // make a fling change only a single page like ViewPager
                .nightMode(false) // toggle night mode
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .defaultPage(0)
                .onRender {
                    testScrollAndZoomToSpecificPlace()
                }
                .onPageScroll { page, positionOffset ->

                    println("@@ did scroll to page $page with TOTAL offset $positionOffset%")
                }
                .spacing(8)
                .enableAnnotationRendering(true)
                .load()
        }
    }

    private fun testScrollAndZoomToSpecificPlace() {
        // TEST INPUTS
        val pageNumber = 5 // 0 indexed
        val xPercent = 1.0f
        val yPercent = 1.0f
        val zoomScale = 2.0f

        testScrollAndZoomToSpecificPlace(pageNumber, xPercent, yPercent, zoomScale)
    }

    fun testScrollAndZoomToSpecificPlace(page: Int, xPercent: Float, yPercent: Float, zoomScale: Float) {
        lifecycleScope.launch {

            println("@@ Scroll & Zoom to page ${page+1}")

//            var yOffsetOfPreviousPages = 0f
//            for (x in 0 until page) {
//                val pageSize = binding.pdfView.getPageSize(x)
//                yOffsetOfPreviousPages += pageSize.height
//                yOffsetOfPreviousPages += binding.pdfView.spacingPx
//                println("@@ did add height ${pageSize.height} from page $x")
//            }


            // try to zoom into the exact center of some page
//            val thisPageSize = binding.pdfView.getPageSize(page)
//            val xOffset = xPercent * thisPageSize.width
//            val thisYOffset = (yPercent * thisPageSize.height)
//            val yOffset = yOffsetOfPreviousPages + thisYOffset
//            println("@@ did add height $thisYOffset from page $page")
//            println("@@ final yOffset: $yOffset")


            //doesnt work
//            binding.pdfView.zoomWithAnimation(xOffset, yOffset, zoomScale)


            //set page offset
            //NOTE: CANT SCROLL TO EXACT PAGE. maybe this is a lost cause
            val offsetPerPage = 1.0 / binding.pdfView.pageCount.toDouble()                              //should be 0.010526316%
            val positionOffset = page * offsetPerPage                                                   //should be 0.05263158 for page 6
            binding.pdfView.positionOffset = positionOffset.toFloat()
            println("@@ Did scroll to offset:$positionOffset with offsetPerPage: $offsetPerPage")

            //zoom in
//            val point = PointF(xPercent, yPercent)
//            binding.pdfView.zoomCenteredRelativeTo(zoomScale, point)
        }
    }
}
