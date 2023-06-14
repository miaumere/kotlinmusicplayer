package com.example.myapplication

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FileListAdapter
import com.example.myapplication.adapters.models.FileModel

class FileListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileListAdapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val files = retrieveFilesAndFolders()
        fileListAdapter = FileListAdapter(files)
        recyclerView.adapter = fileListAdapter
    }

    private fun retrieveFilesAndFolders(): List<FileModel> {
        val files = ArrayList<FileModel>()

        val rootDir = Environment.getExternalStorageDirectory()
        val rootFiles = rootDir.listFiles()

        if (rootFiles != null) {
            for (file in rootFiles) {
                val fileName = file.name
                val filePath = file.absolutePath
                val isFolder = file.isDirectory

                files.add(FileModel(fileName, filePath, isFolder))
            }
        }

        return files
    }
}