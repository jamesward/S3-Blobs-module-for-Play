package play.modules.s3blobs;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;
import org.hibernate.engine.spi.SessionImplementor;

import play.db.Model.BinaryField;
import play.libs.Codec;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

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
		key = s3Key;
	}

	@Override
	public InputStream get() {
		S3Object s3Object = s3Client.getObject(bucket, key);
		return s3Object.getObjectContent();
	}

	@Override
	public void set(InputStream is, String type) {
		bucket = s3Bucket;
		key = (keyPrefix != null ? keyPrefix : "") + Codec.UUID();
		ObjectMetadata om = new ObjectMetadata();
		om.setContentType(type);
		if (contentLength != 0) {
			om.setContentLength(contentLength);
		}
		if (serverSideEncryption) {
			om.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
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
	public boolean equals(Object o, Object o1) throws HibernateException {
		return o == null ? false : o.equals(o1);
	}

	@Override
	public int hashCode(Object o) throws HibernateException {
		return o.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
		String val = StringType.INSTANCE.nullSafeGet(rs, names[0], session);
		if (val == null || val.length() == 0 || !val.contains("|")) {
			return new S3Blob();
		}
		return new S3Blob(val.split("[|]")[0], val.split("[|]")[1]);
	}

	@Override
	public void nullSafeSet(PreparedStatement ps, Object o, int i, SessionImplementor session) throws HibernateException, SQLException {
		if (o != null) {
			ps.setString(i, ((S3Blob) o).bucket + "|" + ((S3Blob) o).key);
		} else {
			ps.setNull(i, Types.VARCHAR);
		}
	}

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
			return null;
		}
		return new S3Blob(bucket, key);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Serializable disassemble(Object o) throws HibernateException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object assemble(Serializable srlzbl, Object o) throws HibernateException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
	    if (original == null) {
	        return null;
	    }
	    if (original instanceof S3Blob) {
	        S3Blob originalBlob = (S3Blob) original;
	        return new S3Blob(originalBlob.bucket, originalBlob.key);
	    }
	    return original;
	}
}
