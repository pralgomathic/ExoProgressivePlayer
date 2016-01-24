/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipvision.ringplayer.ringprogressiveplayer.player;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.util.Log;


import com.ipvision.ringplayer.ringprogressiveplayer.audio.AudioCapabilities;
import com.ipvision.ringplayer.ringprogressiveplayer.extractor.mp4.Mp4Extractor;
import com.ipvision.ringplayer.ringprogressiveplayer.util.MediaCodecAudioTrackRenderer;
import com.ipvision.ringplayer.ringprogressiveplayer.util.MediaCodecVideoTrackRenderer;
import com.ipvision.ringplayer.ringprogressiveplayer.util.TrackRenderer;

import com.ipvision.ringplayer.ringprogressiveplayer.player.RingDemoPlayer.RendererBuilder;
import com.ipvision.ringplayer.ringprogressiveplayer.extractor.Extractor;
import com.ipvision.ringplayer.ringprogressiveplayer.extractor.ExtractorSampleSource;

import com.ipvision.ringplayer.ringprogressiveplayer.upstream.Allocator;
import com.ipvision.ringplayer.ringprogressiveplayer.upstream.DataSource;
import com.ipvision.ringplayer.ringprogressiveplayer.upstream.DefaultAllocator;
import com.ipvision.ringplayer.ringprogressiveplayer.upstream.DefaultBandwidthMeter;
import com.ipvision.ringplayer.ringprogressiveplayer.upstream.DefaultUriDataSource;

/**
 * A {@link RendererBuilder} for streams that can be read using an {@link Extractor}.
 */
public class ExtractorRendererBuilder implements RendererBuilder {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int BUFFER_SEGMENT_COUNT = 256;

  private final Context context;
  private final String userAgent;
  private final Uri uri;
  private static final String TAG = "ExtractorRendererBuilder";
  int count = 0;

  public ExtractorRendererBuilder(Context context, String userAgent, Uri uri) {
    this.context = context;
    this.userAgent = userAgent;
    this.uri = uri;
  }

  @Override
  public void buildRenderers(RingDemoPlayer player) {

    Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(player.getMainHandler(), null);
    DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    Log.d("ExtractorRendererBuilder", "Video URI : " + uri);
    ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
        BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, new Mp4Extractor());
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
        sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, player.getMainHandler(),
        player, 50);

    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
            null, true, player.getMainHandler(), player, AudioCapabilities.getCapabilities(context));

    // Invoke the callback.
    TrackRenderer[] renderers = new TrackRenderer[RingDemoPlayer.RENDERER_COUNT];
    renderers[RingDemoPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[RingDemoPlayer.TYPE_AUDIO] = audioRenderer;
    player.onRenderers(renderers, bandwidthMeter);
  }

  @Override
  public void cancel() {
    // Do nothing.
  }

}
