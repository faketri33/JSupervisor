package org.faketri.utils;

import com.sun.security.auth.module.UnixSystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class UnixSocketUtilities {
    private UnixSocketUtilities(){}

    private static final Set<PosixFilePermission> DIR_PERMS = PosixFilePermissions.fromString("rwx------");
    private static final Set<PosixFilePermission> SOCK_PERMS = PosixFilePermissions.fromString("rw-------");


    private static long myUid() throws IOException {
        return new UnixSystem().getUid();
    }

    public static Path socketDir() throws IOException {
        String xdg = System.getenv("XDG_RUNTIME_DIR");
        Path dir = (xdg != null && !xdg.isEmpty())
                ? Path.of(xdg, Constants.UnixServerConfiguration.DIR_NAME)
                : Path.of("/tmp", Constants.UnixServerConfiguration.DIR_NAME + "-" + myUid());

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
}
