// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.example;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.RetryPolicy;

public class HttpPipelineBuilderSample {
    HttpPipeline pipeline = new HttpPipelineBuilder()
        .httpClient(HttpClient.createDefault())
        .policies(new RetryPolicy())
        .build();

}
