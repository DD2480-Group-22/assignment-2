package org.group22.ci;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.group22.utilities.Configuration;
import org.group22.utilities.Helpers;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class AWSFileUploader {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AWSFileUploader.class);
    private final AmazonS3 s3Client;

    /**
     * Creates and initializes a {@code AWSFileUploader} object. The constructor setups the authentication with
     * AWS services and sets the region.
     */
    public AWSFileUploader() {
        AWSCredentials credentials = new BasicAWSCredentials(Configuration.AWS_ACCESS_KEY_ID, Configuration.AWS_SECRET_KEY);

        Regions clientRegion = Regions.fromName(Configuration.S3_BUCKET_REGION);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(clientRegion).build();
    }

    /**
     * Uploads the file specified by {@code fileName} to an AWS bucket.
     *
     * @param fileName the name of the file
     * @return {@code true} if the upload was successful, otherwise {@code false}
     */
    public boolean uploadFile(final String fileName) {
        try {
            final String folderFileName = "reports/" + fileName;
            PutObjectRequest request = new PutObjectRequest(
                    Configuration.BUCKET_NAME,
                    folderFileName,
                    new File(Configuration.PATH_TO_REPORTS + fileName)
            );

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("x-amz-meta-title", fileName);
            request.setMetadata(metadata);
            s3Client.putObject(request);
            Helpers.updatePreviousBuilds(fileName);
            logger.info("Uploaded file: {} to S3 bucket: {} ", fileName, Configuration.BUCKET_NAME);
            return true;
        } catch (AmazonServiceException e) {
            logger.error("Amazon S3 failed to process the file: {}", fileName, e);
        } catch (SdkClientException e) {
            logger.error("Failed to contact Amazon S3 or the client couldn't parse the response from Amazon S3", e);
        }

        return false;
    }

    /**
     * The function creates a {@code Set} of report ids of the reports previously uploaded on AWS.
     *
     * @return a set of report ids
     */
    public Set<String> getReports() {
        try {
            final Set<String> reports = new HashSet<>();
            ObjectListing objectListing = s3Client.listObjects(Configuration.BUCKET_NAME);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                if (objectSummary.getKey().matches("reports/[A-Za-z0-9_-]+.txt")) {
                    reports.add(objectSummary.getKey().split("reports/")[1]);
                }
            }
            return reports;
        } catch (AmazonServiceException e) {
            logger.error("Amazon S3 failed to return list of objects for bucket: {}", Configuration.BUCKET_NAME, e);
        } catch (SdkClientException e) {
            logger.error("Failed to contact Amazon S3 or the client couldn't parse the response from Amazon S3", e);
        }

        return new HashSet<>();
    }
}
