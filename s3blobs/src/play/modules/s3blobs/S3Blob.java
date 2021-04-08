package play.modules.s3blobs;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import play.db.Model.BinaryField;
import play.libs.Codec;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class S3Blob implements BinaryField, UserType, Serializable {

	static String s3Bucket;
	static AmazonS3 s3Client;
	static boolean serverSideEncryption;
	private String bucket;
	private String key;
	private long contentLength;
	private String keyPrefix;

	public S3Blob() {
	}
	
	public S3Blob(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}			
	
	public S3Blob(long contentLength) {
		this.contentLength = contentLength;
	}
	
	public S3Blob(String keyPrefix, long contentLength) {
		this.keyPrefix = keyPrefix;
		this.contentLength = contentLength;
	}	

	private S3Blob(String bucket, String s3Key) {
		this.bucket = bucket;
		this.key = s3Key;
	}

	@Override
	public InputStream get() {
		S3Object s3Object = s3Client.getObject(bucket, key);
		return s3Object.getObjectContent();
	}

	@Override
	public void set(InputStream is, String type) {
		this.bucket = s3Bucket;
		this.key = (keyPrefix != null ? keyPrefix : "") + Codec.UUID();
		ObjectMetadata om = new ObjectMetadata();
		om.setContentType(type);
		if (contentLength != 0) {
			om.setContentLength(contentLength);
		}
		if (serverSideEncryption) {
			om.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
		}
		s3Client.putObject(bucket, key, is, om);
	}

	public void delete () {
		s3Client.deleteObject(s3Bucket, key);
	}
	
	@Override
	public long length() {
		ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
		return om.getContentLength();
	}

	@Override
	public String type() {
		ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
		return om.getContentType();
	}

	@Override
	public boolean exists() {
		if (bucket == null || bucket.isEmpty() || key == null || key.isEmpty()) {
			return false;
		}
		ObjectMetadata om = s3Client.getObjectMetadata(bucket, key);
		return om != null;
	}

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.VARCHAR };
	}

	@Override
	public Class returnedClass() {
		return S3Blob.class;
	}

	@Override
	public boolean equals(Object o, Object o1) {
        return Objects.equals(o, o1);
	}

	@Override
	public int hashCode(Object o) {
		return o.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object o) throws SQLException {
		String val = StringType.INSTANCE.nullSafeGet(rs, names[0], session);

        final Object result;
        if (val == null || val.isEmpty() || "null|null".equals(val)) {
            result = val;
        } else {
            boolean isLegacyKeyWithBucketPrefix = val.contains("|");

            String resolvedKey = isLegacyKeyWithBucketPrefix
                    ? val.split("[|]")[1]
                    : val;

            result = new S3Blob(s3Bucket, resolvedKey);
        }
        return result;
    }

	@Override
	public void nullSafeSet(PreparedStatement ps, Object o, int i, SharedSessionContractImplementor session) throws SQLException {
		if (o != null) {
			ps.setString(i, ((S3Blob) o).key);
		} else {
			ps.setNull(i, Types.VARCHAR);
		}
	}

	@Override
	public Object deepCopy(Object o) {
		if (o == null) {
			return null;
		}
		return new S3Blob(this.bucket, this.key);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object assemble(Serializable srlzbl, Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object replace(Object o, Object o1, Object o2) {
		return o;
	}

	public String getKey() {
		return key;
	}
}
