package com.example.imagescalingdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagescalingdemo.databinding.ImagesRowBinding

class ImageRowAdapter(private val imageRowList: List<ImageRow>) : RecyclerView.Adapter<ImageRowAdapter.ImageViewHolder>() {

    class ImageViewHolder(private val rowBinding: ImagesRowBinding) : RecyclerView.ViewHolder(rowBinding.root) {
        fun bind(imageRow: ImageRow) {
            rowBinding.iv1.setImageBitmap(imageRow.img1)
            rowBinding.iv2.setImageBitmap(imageRow.img2)
            rowBinding.iv3.setImageBitmap(imageRow.img3)
        }

        companion object {
            fun create(parent: ViewGroup) : ImageViewHolder {
                val rowBinding = ImagesRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ImageViewHolder(rowBinding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder.create(parent)
    }

    override fun onBindViewHolder(holderImage: ImageViewHolder, position: Int) {
        val imgRowItem = imageRowList[position]
        holderImage.bind(imgRowItem)
    }

    override fun getItemCount(): Int {
        return imageRowList.size
    }
}