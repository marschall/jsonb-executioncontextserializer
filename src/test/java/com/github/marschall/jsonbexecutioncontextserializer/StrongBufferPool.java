package com.github.marschall.jsonbexecutioncontextserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.parsson.api.BufferPool;

final class StrongBufferPool implements BufferPool {

  private static final int BUFFER_SIZE = 512;

  private final List<char[]> buffers;

  private final ReentrantLock lock;

  StrongBufferPool() {
    this.buffers = new ArrayList<>();
    this.lock = new ReentrantLock();
  }

  @Override
  public char[] take() {
    this.lock.lock();
    try {
      if (this.buffers.isEmpty()) {
        return new char[BUFFER_SIZE];
      }
      return this.buffers.remove(this.buffers.size() - 1);
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void recycle(char[] buf) {
    Objects.requireNonNull(buf);

    this.lock.lock();
    try {

    } finally {
      this.lock.unlock();
    }
  }

}
