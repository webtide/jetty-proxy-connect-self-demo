package org.eclipse.jetty.demo;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jetty.proxy.AsyncMiddleManServlet;
import org.eclipse.jetty.util.TypeUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ShaSumContentTransformer implements AsyncMiddleManServlet.ContentTransformer
{
    private static final Logger LOG = Log.getLogger(ShaSumContentTransformer.class);
    private final String title;
    private final MessageDigest digest;

    public ShaSumContentTransformer(String title)
    {
        this.title = title;
        try
        {
            digest = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Unable to find SHA1 MessageDigest??", e);
        }
    }

    @Override
    public void transform(ByteBuffer input, boolean finished, List<ByteBuffer> output)
    {
        digest.update(input.slice());
        output.add(input);

        if(finished)
        {
            byte[] checksum = digest.digest();
            LOG.info("{} - SHA1 Checksum is {}", title, TypeUtil.toHexString(checksum).toLowerCase(Locale.US));
        }
    }
}
