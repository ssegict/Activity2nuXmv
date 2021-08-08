package ict.generate.util

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Iterator
import java.util.NoSuchElementException

class FileAccess {
	static def writeFile(String path, String content) {
		var wr = getWriter(path)
		wr.append(content)
		wr.close
	}
	
	def static getWriter(String path) {
		if(! new File(path).parentFile.exists) {
			new File(path).parentFile.mkdirs
		}
		return new BufferedWriter(new FileWriter(new File(path)));
	}
	
	static def readFile(String path) {
		return readFile(new File(path))
	}
	
	static def readFile(File file) {
		var fcont = new StringBuilder()
		
		for(line : FileAccess.readFileLine(file)) {
			fcont.append(line).append(System.lineSeparator)
		}
		return fcont.toString
	}
	
	static def readFileLine(String path) {
		return new IterableFileWrapper(path)
	}
	
	static def readFileLine(File file) {
		return new IterableFileWrapper(file)
	}
	
	static class IterableFileWrapper implements Iterable<String> {
		val File file
		
		new(String path) {
			this(new File(path))
		}
		
		new(File file) {
			this.file = file
		}
		
		override iterator() {
			return new IterableFile(file)
		}
	}
	
	static class IterableFile implements Iterator<String> {
		val BufferedReader br
		var String nextBuffer = null
		
		new(File file) {
			br = new BufferedReader(new FileReader(file))
		}
		
		override hasNext() {
			if(nextBuffer !== null) return true
			
			nextBuffer = br.readLine
			return nextBuffer !== null
		}
		
		override next() {
			if(! hasNext()) {
				throw new NoSuchElementException()
			}
			
			var temp = nextBuffer
			nextBuffer = null
			return temp
		}
	}
	
}
