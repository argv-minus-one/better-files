package better.files

import java.io.IOException

import scala.compat.Platform.EOL

/** Signals an error in looking up a class loader resource through [[Resource$ Resource]].
  *
  * This exception may occur if a requested resource is not found, inaccessible from the Java module from which it was requested, or restricted by the security manager. See [[https://docs.oracle.com/javase/10/docs/api/java/lang/Class.html#getResource(java.lang.String) Class#getResource]] for details.
  *
  * @param name The name of the requested resource.
  * @param cause The cause of this exception, if any.
  */
class ResourceLookupException(name: String, cause: Throwable = null) extends IOException(name, cause) {
  override def getLocalizedMessage =
    s"Failed to load resource: $getMessage${EOL}It may be missing, inaccessible from here, or restricted by the security manager."
}
