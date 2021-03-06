/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.implcore.tags;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import io.opencensus.tags.TaggingState;
import io.opencensus.tags.TagsComponent;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The current {@link TaggingState} for a {@link TagsComponent}.
 *
 * <p>This class allows different tagging classes to share the state in a thread-safe way.
 */
@ThreadSafe
public final class CurrentTaggingState {

  @GuardedBy("this")
  private TaggingState currentState = TaggingState.ENABLED;

  @GuardedBy("this")
  private boolean isRead;

  public synchronized TaggingState get() {
    isRead = true;
    return getInternal();
  }

  public synchronized TaggingState getInternal() {
    return currentState;
  }

  // Sets current state to the given state.
  synchronized void set(TaggingState state) {
    Preconditions.checkState(!isRead, "State was already read, cannot set state.");
    currentState = checkNotNull(state, "state");
  }
}
