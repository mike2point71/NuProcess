package com.zaxxer.nuprocess.okioconvert;

import okio.Buffer;
import okio.Source;
import okio.Timeout;

import java.nio.ByteBuffer;

public class ByteBufferSource implements Source {
  final ByteBuffer buffer;
  final Buffer.UnsafeCursor cursor = new Buffer.UnsafeCursor();

  public ByteBufferSource(ByteBuffer buffer) {
    this.buffer = buffer.asReadOnlyBuffer(); // Maintain separate position
  }

  @Override
  public long read(Buffer sink, long byteCount) {
    if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    if (byteCount == 0) return 0;
    if (buffer.remaining() == 0) return -1;
    byteCount = (int) Math.min(byteCount, buffer.remaining());

    int remaining = (int) byteCount;
    sink.readAndWriteUnsafe(cursor);
    try {
      cursor.seek(sink.size());
      cursor.resizeBuffer(sink.size() + byteCount);

      while (remaining > 0) {
        int toRead = Math.min(remaining, cursor.end - cursor.start);
        buffer.get(cursor.data, cursor.start, toRead);
        remaining -= toRead;
        cursor.next();
      }
    } finally {
      // Truncate sink to the data that was actually written
      cursor.resizeBuffer(sink.size() - remaining);
      cursor.close();
    }
    return byteCount;
  }

  @Override
  public Timeout timeout() {
    return Timeout.NONE;
  }

  @Override
  public void close() {
  }
}
