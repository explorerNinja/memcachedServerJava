/**
 * @author Dilip Simha
 */
package server.commands;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import server.CacheManager;
import server.cache.CacheValue;

/**
 * Process the set command end-to-end.
 * Upon fetching the necessary item from client socket, write it out to cache.
 */
public class CommandSet extends AbstractCommand {
    private final static Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    public CommandSet(final CacheManager cacheManager,
            SocketChannel socketChannel) {
        super(cacheManager, socketChannel);
    }

    public void process(CommandSetRequest request, byte[] data)
            throws IOException, InterruptedException {
        CacheValue value = new CacheValue(request.flags, request.bytes, data);
        
        // Store the key in whatever format, it doesn;t matter. But make sure
        // to use the same format consistently in the server.
        String key = new String(request.key, AbstractCommand.charset);

        boolean cached = cache.set(key, value);
        if (cached) {
            respondToClient(CommandSetResponse.STORED);
        } else {
            respondToClient(CommandSetResponse.NOT_STORED);
        }
        LOGGER.info("Responded to client for key: " + request.key);
    }

    private void respondToClient(CommandSetResponse response)
            throws IOException {
        LOGGER.info("responding to client...");

        CharBuffer charBuf;
        if (response == CommandSetResponse.STORED) {
            charBuf = CharBuffer.wrap("STORED" + "\r\n");
        } else {
            charBuf = CharBuffer.wrap("NOTSTORED" + "\r\n");
        }
        ByteBuffer byteBuf = charset.encode(charBuf);
        writeToSocket(byteBuf);
        byteBuf = null;
    }
}
