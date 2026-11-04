package millbuild

import coursier.cache.{ArchiveCache, ArtifactError}
import coursier.util.Artifact

import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import java.util.Locale

/** Downloads the native coursier launcher (`cs`) of [[Versions.coursier]], so that the build
  * doesn't need one to be installed already.
  *
  * The launcher is fetched from the coursier GitHub releases and extracted with coursier's
  * [[ArchiveCache]], which keeps it in the coursier archive cache (`~/.cache/coursier/arc` on
  * Linux) and only downloads it once.
  */
object CoursierLauncher {

  // Names of the launchers of the coursier GitHub releases, minus their extension
  private def launcherName: String = {
    val os = sys.props("os.name").toLowerCase(Locale.ROOT)
    val arch = sys.props("os.arch").toLowerCase(Locale.ROOT) match {
      case "amd64" | "x86_64" => "x86_64"
      case "aarch64" | "arm64" => "aarch64"
      case other => sys.error(s"No coursier launcher for the $other architecture")
    }
    if (os.contains("windows"))
      // There's no native Windows ARM launcher, the x86_64 one runs under emulation there
      "cs-x86_64-pc-win32"
    else if (os.contains("linux")) s"cs-$arch-pc-linux"
    else if (os.contains("mac")) s"cs-$arch-apple-darwin"
    else sys.error(s"No coursier launcher for the $os OS")
  }

  /** Path to the coursier launcher, downloaded first if it's not in the archive cache already */
  def get(): os.Path = {
    val isWindows = scala.util.Properties.isWin
    val name = launcherName
    val ext = if (isWindows) "zip" else "gz"
    val url =
      s"https://github.com/coursier/coursier/releases/download/v${Versions.coursier}/$name.$ext"

    val archiveCache = ArchiveCache()
    val extracted = archiveCache.get(Artifact(url))
      .unsafeRun()(using archiveCache.cache.ec) match {
      case Left(err: ArtifactError) =>
        throw new Exception(s"Error downloading coursier launcher from $url", err)
      case Right(f) => os.Path(f)
    }

    // Windows launchers are in a ZIP archive, which gets extracted as a directory,
    // and the others are GZIP-ed files, which get decompressed as a file
    val launcher =
      if (isWindows) extracted / s"$name.exe"
      else extracted
    if (!os.exists(launcher))
      sys.error(s"$launcher not found (downloaded from $url)")
    // Decompressed GZIP files aren't executable by default
    if (!isWindows && !os.perms(launcher).contains(OWNER_EXECUTE))
      os.perms.set(launcher, os.perms(launcher) + OWNER_EXECUTE)
    launcher
  }
}
