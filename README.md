Build the s3blobs Play Framework Module
---------------------------------------

    play deps --sync
    play build-module

Due to how Play framework's dependencies loads 'modules', you need to build the module and then commit the built zip file to the /dist folder before it can be used. 

# Setup

Install `ant` build tool:

    brew install ant
