package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FileListAdapter
import com.example.myapplication.adapters.models.FileModel
import java.io.File

class FileListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileListAdapter: FileListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val files = retrieveFilesAndFolders()
        fileListAdapter = FileListAdapter(retrieveFilesAndFolders()) { clickedFile ->
            if (clickedFile.isFolder) {
                openFolderContents(clickedFile)
            } else {
                openFile(clickedFile)
            }
        }
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

    private fun openFolderContents(folder: FileModel) {
        val folderContents = retrieveFolderContents(folder.path)

        fileListAdapter.files = folderContents
        fileListAdapter.notifyDataSetChanged()
    }

    private fun openFile(file: FileModel) {
        val intent = Intent(this, PlayMusicActivity::class.java)
        intent.putExtra("filePath", file.path)
        startActivity(intent)
    }

    private fun retrieveFolderContents(folderPath: String): List<FileModel> {
        val folder = File(folderPath)
        val folderContents = mutableListOf<FileModel>()

        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()

            if (files != null) {
                for (file in files) {
                    val fileName = file.name
                    val filePath = file.absolutePath
                    val isFolder = file.isDirectory

                    folderContents.add(FileModel(fileName, filePath, isFolder))
                }
            }
        }

        return folderContents
    }

}