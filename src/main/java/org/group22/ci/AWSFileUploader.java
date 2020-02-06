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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AWSFileUploader {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AWSFileUploader.class);
    private final AmazonS3 s3Client;
    private static final String CONNECTION_ERROR = "Failed to contact AWS S3 or the client couldn't parse the response from Amazon S3";
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

    public void upload(final String fileName) {
        uploadFile(fileName);
        uploadHTML(fileName);
    }

    /**
     * Uploads the file specified by {@code fileName} to an AWS bucket.
     *
     * @param fileName the name of the file
     * @return {@code true} if the upload was successful, otherwise {@code false}
     */
    public boolean uploadHTML(final String fileName) {
        try {
            Helpers.txtToHTMLFile(fileName);
            final String folderFileName = "reports_html/" + fileName + ".html";
            PutObjectRequest request = new PutObjectRequest(
                    Configuration.BUCKET_NAME,
                    folderFileName,
                    new File(Configuration.PATH_TO_REPORTS_HTML + fileName + ".html")
            );

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/html");
            metadata.addUserMetadata("x-amz-meta-title", fileName);
            request.setMetadata(metadata);
            s3Client.putObject(request);
            Helpers.updatePreviousBuilds(fileName);
            logger.info("Uploaded file: {} to S3 bucket: {} ", fileName, Configuration.BUCKET_NAME);
            return true;
        } catch (AmazonServiceException e) {
            logger.error("Amazon S3 failed to process the file: {}", fileName, e);
        } catch (SdkClientException e) {
            logger.error(CONNECTION_ERROR, e);
        } catch (IOException e) {
            logger.error("Failed to create HTML report", e);
        }

        return false;
    }

    /**
     * Uploads the file specified by {@code fileName} to an AWS bucket.
     *
     * @param fileName the name of the file
     * @return {@code true} if the upload was successful, otherwise {@code false}
     */
    public boolean uploadFile(final String fileName) {
        try {
            final String folderFileName = "reports/" + fileName + ".txt";
            PutObjectRequest request = new PutObjectRequest(
                    Configuration.BUCKET_NAME,
                    folderFileName,
                    new File(Configuration.PATH_TO_REPORTS + fileName + ".txt")
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
            logger.error(CONNECTION_ERROR, e);
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
