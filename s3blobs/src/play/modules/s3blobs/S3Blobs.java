package play.modules.s3blobs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import play.Logger;
import play.Play;
import play.PlayPlugin;

public class S3Blobs extends PlayPlugin {

	@Override
	public void onApplicationStart() {
		if (!ConfigHelper.getBoolean("s3.storage.enabled", true)) {
			Logger.info("S3Blobs module disabled");
			return;
		}		

		Logger.info("Starting the S3Blobs module");
		S3Blob.s3Bucket = Play.configuration.getProperty("s3.bucket");
		S3Blob.serverSideEncryption = ConfigHelper.getBoolean("s3.useServerSideEncryption", true);

		boolean useLocalServer = ConfigHelper.getBoolean("s3.useLocalServer", false);
		if (useLocalServer) {
			String endpoint = Play.configuration.getProperty("s3.endpoint");
			String accessKey = Play.configuration.getProperty("s3.localAccessKey");
			String secretKey = Play.configuration.getProperty("s3.localSecretKey");
			Logger.info("Using S3 endpoint ["+ endpoint +"]");

			AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
			AWSCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);

			AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, "Region");

			S3Blob.s3Client = AmazonS3Client.builder()
					.withCredentials(provider)
					.withEndpointConfiguration(endpointConfiguration)
					.build();
		} else {
			S3Blob.s3Client = AmazonS3Client.builder().build();
		}

	}
}
