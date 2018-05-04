package better.files
package test_pkg

class ResourceSpecHelper {
  def myTestFile = Resource.my.asFile("another-test-file.txt")

  def nonexistentTestFile = Resource.my.asFile("nonexistent-file.txt")
}
