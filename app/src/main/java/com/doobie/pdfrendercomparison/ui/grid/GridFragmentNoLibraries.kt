package com.doobie.pdfrendercomparison.ui.grid

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.*
import androidx.fragment.app.Fragment
import com.doobie.pdfrendercomparison.R
import com.doobie.pdfrendercomparison.databinding.FragmentPdfGridBinding
import com.doobie.pdfrendercomparison.getPdfFile

class GridFragmentNoLibraries : Fragment() {

    private lateinit var binding: FragmentPdfGridBinding

    lateinit var page: PdfRenderer.Page
    lateinit var pdfRender: PdfRenderer
    lateinit var bitmap: Bitmap

    var pageIndex = 0
    var xOffset = 0f
    var yOffset = 0f
    var zoomScale = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.change_page_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.next_page) {
            pageIndex++
            xOffset = 0f
            yOffset = 0f
            zoomScale = 1f
            loadPage()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPdfGridBinding.inflate(inflater)
        val step = 100
        binding.buttonLeft.setOnClickListener {
            xOffset += (step / zoomScale)
            loadPage()
        }
        binding.buttonRight.setOnClickListener {
            xOffset -= (step / zoomScale)
            loadPage()
        }
        binding.buttonUp.setOnClickListener {
            yOffset += (step / zoomScale)
            loadPage()
        }
        binding.buttonDown.setOnClickListener {
            yOffset -= (step / zoomScale)
            loadPage()
        }
        binding.buttonZoomin.setOnClickListener {
            zoomScale *= 2f

            val addedXOffset = bitmap.width / (.5f * zoomScale * getViewToPageScale())
            val addedYOffset = bitmap.height / (.5f * zoomScale * getViewToPageScale())
            xOffset -= addedXOffset
            yOffset -= addedYOffset
            loadPage()
        }
        binding.buttonZoomout.setOnClickListener {
            zoomScale /= 2f

            val addedXOffset = bitmap.width / (zoomScale * getViewToPageScale())
            val addedYOffset = bitmap.height / (zoomScale * getViewToPageScale())
            xOffset += addedXOffset
            yOffset += addedYOffset
            loadPage()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadPdf()
        loadPage()
    }

    private fun loadPdf() {
        val file = getPdfFile(requireContext())

        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRender = PdfRenderer(fileDescriptor)
    }

    private fun loadPage() {

        page = pdfRender.openPage(pageIndex)

        // Hacky bitmap sizes. Ideally these would be pulled from the view itself but I couldnt get measure() to work
        val width = resources.displayMetrics.widthPixels
        val aspectRatio = page.height.toDouble() / page.width.toDouble()
        val height = (width * aspectRatio).toInt()  //must max to doc size

        // Create bitmap to render on
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(-0x1) //sets default bg color to white

        // Create matrix for translating the page onto the bitmap according to our offset and zoom
        val matrix = Matrix()
        matrix.setScale(zoomScale* getViewToPageScale(), zoomScale* getViewToPageScale())   //positive scale zooms in, but we are still starting at the top left,,,
        matrix.postTranslate(zoomScale * xOffset , zoomScale * yOffset)

        // Render & set image
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        binding.imageView.setImageBitmap(bitmap)

        //finally
        page.close()
    }

    private fun getViewToPageScale(): Float {
        return resources.displayMetrics.widthPixels.toFloat() / page.width.toFloat()
    }

}
