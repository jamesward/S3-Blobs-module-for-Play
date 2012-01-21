Use the s3blobs Play Framework Module
-------------------------------------

    play install s3blobs

    export S3_BUCKET=com.unique.s3.bucket.id
    export AWS_ACCESS_KEY=<Your AWS Access Key>
    export AWS_SECRET_KEY=<Your AWS Secret Key>

    play run


Build the s3blobs Play Framework Module
---------------------------------------

    play deps --sync
    play build-module
