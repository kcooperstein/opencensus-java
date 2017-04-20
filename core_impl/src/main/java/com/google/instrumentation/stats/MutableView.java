/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import com.google.instrumentation.common.Function;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A mutable version of {@link View}, used for recording stats and start/end time.
 */
// TODO(songya): remove or modify the methods of this class, since it's not part of the API.
abstract class MutableView {

  /**
   * The {@link ViewDescriptor} associated with this {@link View}.
   */
  abstract ViewDescriptor getViewDescriptor();

  /**
   * Applies the given match function to the underlying data type.
   */
  abstract <T> T match(
      Function<MutableDistributionView, T> p0,
      Function<MutableIntervalView, T> p1);

  /**
   * Record stats with the given tags.
   */
  abstract void record(Map<String, String> tags, double value);

  /**
   * Convert this {@link MutableView} to {@link View}.
   */
  abstract View toView();

  private MutableView() {
  }

  /**
   * A {@link MutableView} for recording stats on distribution-based aggregations.
   */
  static final class MutableDistributionView extends MutableView {

    /**
     * Constructs a new {@link MutableDistributionView}.
     */
    static MutableDistributionView create(DistributionViewDescriptor distributionViewDescriptor,
        List<MutableDistribution> distributions, Timestamp start) {
      return new MutableDistributionView(
          distributionViewDescriptor, distributions, start);
    }

    @Override
    ViewDescriptor getViewDescriptor() {
      return distributionViewDescriptor;
    }

    @Override
    <T> T match(
        Function<MutableDistributionView, T> p0, Function<MutableIntervalView, T> p1) {
      return p0.apply(this);
    }

    @Override
    void record(Map<String, String> tags, double value) {
      // TODO(songya): compare tags with tagKeys in ViewDescriptor
      for (MutableDistribution distribution : mutableDistributions) {
        distribution.add(value);
      }
    }

    @Override
    final View toView() {
      final List<DistributionAggregation> distributionAggregations =
          new ArrayList<DistributionAggregation>();
      for (MutableDistribution distribution : mutableDistributions) {
        distributionAggregations.add(
            DistributionAggregation.create(distribution.getCount(), distribution.getMean(),
                distribution.getSum(),
                null, null,  // TODO(songya): convert Range and generate List<Tag>
                distribution.getBucketCounts()));
      }
      return DistributionView.create(distributionViewDescriptor, distributionAggregations, start,
          Timestamp.fromMillis(System.currentTimeMillis()));
    }

    /**
     * Returns start timestamp for this aggregation.
     */
    Timestamp getStart() {
      return start;
    }

    private final DistributionViewDescriptor distributionViewDescriptor;
    // TODO(songya): change to Map<List<TagValue>, Distribution> to record Tag Values.
    private final List<MutableDistribution> mutableDistributions;
    private final Timestamp start;

    private MutableDistributionView(DistributionViewDescriptor distributionViewDescriptor,
        List<MutableDistribution> mutableDistributions, Timestamp start) {
      this.distributionViewDescriptor = distributionViewDescriptor;
      this.mutableDistributions = mutableDistributions;
      this.start = start;
    }
  }

  /**
   * A {@link MutableView} for recording stats on interval-based aggregations.
   */
  static final class MutableIntervalView extends MutableView {

    /**
     * Constructs a new {@link MutableIntervalView}.
     */
    static MutableIntervalView create(IntervalViewDescriptor viewDescriptor) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    ViewDescriptor getViewDescriptor() {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    <T> T match(
        Function<MutableDistributionView, T> p0, Function<MutableIntervalView, T> p1) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    void record(Map<String, String> tags, double value) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    final View toView() {
      throw new UnsupportedOperationException("Not implemented.");
    }
  }
}