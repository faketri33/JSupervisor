package org.faketri.net.socket;

import com.sun.security.auth.module.UnixSystem;
import org.faketri.utils.Constants;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

class UnixSocketUtilities {
    static final Set<PosixFilePermission> DIR_PERMS = PosixFilePermissions.fromString("rwx------");
    static final Set<PosixFilePermission> SOCK_PERMS = PosixFilePermissions.fromString("rw-------");

    private UnixSocketUtilities() {
    }

    private static long myUid() {
        return new UnixSystem().getUid();
    }

    private static Path baseDir() {
        String xdg = System.getenv("XDG_RUNTIME_DIR");
        if (xdg != null && !xdg.isEmpty()) {
            return Path.of(xdg);
        }
        return Path.of(System.getProperty("user.home"), ".local", "state");
    }

    private static Path socketDir() throws IOException {
        Path dir = baseDir().resolve(Constants.UnixServerConfiguration.DIR_NAME);

        try {
            // mode is set atomically at creation (only narrowed by umask, never widened)
            Files.createDirectory(dir, PosixFilePermissions.asFileAttribute(DIR_PERMS));
        } catch (FileAlreadyExistsException ignored) {
            // someone created it first: could be us on a previous run, or an attacker. Verify below.
        }

        // NOFOLLOW_LINKS: a symlink is reported as "not a directory"
        PosixFileAttributes a = Files.readAttributes(dir, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        int owner = (Integer) Files.getAttribute(dir, "unix:uid", LinkOption.NOFOLLOW_LINKS);
        if (!a.isDirectory() || owner != myUid() || !a.permissions().equals(DIR_PERMS)) {
            throw new IOException("Refusing to use unsafe socket directory: " + dir);
        }
        return dir;
    }

    protected static UnixDomainSocketAddress address() throws IOException {
        Path sock = UnixSocketUtilities.socketDir().resolve(Constants.UnixServerConfiguration.SOCK_NAME);
        Files.deleteIfExists(sock);
        return UnixDomainSocketAddress.of(sock);
    }

    protected static void installDirectoryPermissions(Path path) throws IOException {
        Files.setPosixFilePermissions(path, UnixSocketUtilities.SOCK_PERMS);
    }

}
