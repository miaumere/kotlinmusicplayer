package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.adapters.models.FileModel
import androidx.recyclerview.widget.RecyclerView

class FileListAdapter(files: List<FileModel>, private val onItemClick: (FileModel) -> Unit) :
    RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    private val mutableFiles: MutableList<FileModel> = files.toMutableList()

    var files: List<FileModel>
        get() = mutableFiles
        set(value) {
            mutableFiles.clear()
            mutableFiles.addAll(value)
        }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        private val musicIconImageView: ImageView = itemView.findViewById(R.id.musicIconImageView)
        private val folderIconImageView: ImageView = itemView.findViewById(R.id.folderIconImageView)
        fun bind(file: FileModel) {
            fileNameTextView.text = file.name

            if (!file.isFolder) {
                musicIconImageView.visibility = View.VISIBLE
                folderIconImageView.visibility = View.GONE
            } else {
                musicIconImageView.visibility = View.GONE
                folderIconImageView.visibility = View.VISIBLE

            }

            itemView.setOnClickListener {
                onItemClick.invoke(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int {
        return files.size
    }
}