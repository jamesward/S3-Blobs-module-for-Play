package play.modules.s3blobs;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.exceptions.ConfigurationException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

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
		S3Blob.s3Client = new AmazonS3Client();
	}
}
