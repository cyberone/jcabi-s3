/**
 * Copyright (c) 2012-2013, JCabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Amazon S3 bucket.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "bkt", "name" })
@Loggable(Loggable.DEBUG)
final class AwsOcket implements Ocket {

    /**
     * Bucket we're in.
     */
    private final transient Bucket bkt;

    /**
     * Object name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param bucket Bucket name
     * @param obj Object name
     */
    AwsOcket(final Bucket bucket, final String obj) {
        this.bkt = bucket;
        this.name = obj;
    }

    @Override
    public Bucket bucket() {
        return this.bkt;
    }

    @Override
    public String key() {
        return this.name;
    }

    @Override
    public ObjectMetadata meta() {
        final AmazonS3 aws = this.bkt.region().aws();
        return aws.getObjectMetadata(
            new GetObjectMetadataRequest(this.bkt.name(), this.name)
        );
    }

    @Override
    public void read(final OutputStream output) throws IOException {
        final AmazonS3 aws = this.bkt.region().aws();
        try {
            final S3Object obj = aws.getObject(
                new GetObjectRequest(this.bkt.name(), this.name)
            );
            final InputStream input = obj.getObjectContent();
            IOUtils.copy(input, output);
            input.close();
        } catch (AmazonS3Exception ex) {
            throw new OcketNotFoundException(
                String.format(
                    "ocket '%s' not found in '%s'", this.name, this.bkt.name()
                ),
                ex
            );
        }
    }

    @Override
    public void write(final InputStream input, final ObjectMetadata meta)
        throws IOException {
        final AmazonS3 aws = this.bkt.region().aws();
        aws.putObject(
            new PutObjectRequest(this.bkt.name(), this.name, input, meta)
        );
        input.close();
    }

}
