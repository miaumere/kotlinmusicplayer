package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.adapters.models.FileModel
import androidx.recyclerview.widget.RecyclerView

class FileListAdapter(private val files: List<FileModel>) :
    RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(file: FileModel) {
            // Bind data to the views in the item layout
            val fileNameTextView = itemView.findViewById<TextView>(R.id.fileNameTextView)
            fileNameTextView.text = file.name

            // Set click listener for the item
            itemView.setOnClickListener {
                if (file.isFolder) {
                    // Handle folder click
                    // Navigate to the folder
                } else {
                    // Handle file click
                    // Play the file
                }
            }
        }
    }
}