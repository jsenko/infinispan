/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2000 - 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.infinispan.util.concurrent.locks.containers;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * A container for locks.  Used with lock striping.
 *
 * @author Manik Surtani (<a href="mailto:manik@jboss.org">manik@jboss.org</a>)
 * @since 4.0
 */
@ThreadSafe
public abstract class AbstractStripedLockContainer implements LockContainer {
   private int lockSegmentMask;
   private int lockSegmentShift;


   final int calculateNumberOfSegments(int concurrencyLevel) {
      int tempLockSegShift = 0;
      int numLocks = 1;
      while (numLocks < concurrencyLevel) {
         ++tempLockSegShift;
         numLocks <<= 1;
      }
      lockSegmentShift = 32 - tempLockSegShift;
      lockSegmentMask = numLocks - 1;
      return numLocks;
   }

   final int hashToIndex(Object object) {
      return (hash(object) >>> lockSegmentShift) & lockSegmentMask;
   }

   /**
    * Returns a hash code for non-null Object x. Uses the same hash code spreader as most other java.util hash tables,
    * except that this uses the string representation of the object passed in.
    *
    * @param object the object serving as a key
    * @return the hash code
    */
   final int hash(Object object) {
      int h = object.hashCode();
//      h ^= (h >>> 20) ^ (h >>> 12);
//      return h ^ (h >>> 7) ^ (h >>> 4);

      h += ~(h << 9);
      h ^= (h >>> 14);
      h += (h << 4);
      h ^= (h >>> 10);
      return h;

   }

   protected abstract void initLocks(int numLocks);

   public Lock acquireLock(Object key, long timeout, TimeUnit unit) throws InterruptedException {
      Lock lock = getLock(key);
      return lock.tryLock(timeout, unit) ? lock : null;
   }

   public void releaseLock(Object key) {
      getLock(key).unlock();
   }
}
