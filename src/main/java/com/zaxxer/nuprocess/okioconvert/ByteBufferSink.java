package com.zaxxer.nuprocess.okioconvert;

import okio.Buffer;
import okio.Sink;
import okio.Timeout;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferSink implements Sink {
  final ByteBuffer buffer;
  final Buffer.UnsafeCursor cursor = new Buffer.UnsafeCursor();

  public ByteBufferSink(ByteBuffer buffer) {
    this.buffer = buffer.duplicate(); // Maintain separate position
  }

  @Override
  public void write(Buffer source, long byteCount) throws IOException {
    if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    if (byteCount == 0) return;
    if (byteCount + buffer.position() > buffer.limit())
      throw new IOException(String.format("overflow: byteCount=%d position=%d limit=%d",
        byteCount, buffer.position(), buffer.limit()));

    source.readUnsafe(cursor);
    try {
      long remaining = byteCount;
      while (remaining > 0) {
        cursor.next();
        int toWrite = (int) Math.min(remaining, cursor.end - cursor.start);
        buffer.put(cursor.data, cursor.start, toWrite);
        remaining -= toWrite;
      }
    } finally {
      cursor.close();
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public Timeout timeout() {
    return Timeout.NONE;
  }

  @Override
  public void close() {
  }
}
