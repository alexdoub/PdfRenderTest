package com.doobie.pdfrendercomparison.ui.scrolllist

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

    class ZoomOperation(val page: Int, val xPercent: Float, val yPercent: Float, val scale: Float)

    var queuedZoom : ZoomOperation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScrollListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val somePageNumber = 82 // zero indexed
        binding.jumpButton.setOnClickListener {
            val xPercent = .5f
            val yPercent = .5f
            val zoomScale = 2.0f

            jumpTo(somePageNumber, xPercent, yPercent, zoomScale)
        }
        binding.jumpButton.text = "Jump to page ${somePageNumber + 1}"

        binding.pdfView.fromFile(getPdfFile(requireContext()))
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
                binding.jumpButton.isEnabled = true
            }
            .onPageScroll { page, positionOffset ->

                // So hacky... but we cant change page & zoom at the same time. we must wait for the page to load
                if (page == queuedZoom?.page) {
                    val zoomToDo = queuedZoom!!
                    queuedZoom = null

                    lifecycleScope.launch {
                        delay(500)  //eww... but we must wait for the page to ACTUALLY load
                        val xOffset = zoomToDo.xPercent * binding.pdfView.width
                        val yOffset = zoomToDo.yPercent * binding.pdfView.height
                        binding.pdfView.zoomWithAnimation(xOffset, yOffset, zoomToDo.scale)
                    }
                }
            }
            .onLoad { page ->

            }
            .spacing(8)
            .enableAnnotationRendering(true)
            .load()
    }

    fun jumpTo(page: Int, xPercent: Float, yPercent: Float, zoomScale: Float) {

        binding.pdfView.resetZoom() // must reset zoom BEFORE jump to page or it interrupts jumpTo. Also, we must reset zoom because if we're already zoomed in then zoomWithAnimation doesnt work lololol
        binding.pdfView.jumpTo(page, true)

        // Queue up the zoom-in. See notes in onPageScroll
        queuedZoom = ZoomOperation(page, xPercent, yPercent, zoomScale)
    }
}
