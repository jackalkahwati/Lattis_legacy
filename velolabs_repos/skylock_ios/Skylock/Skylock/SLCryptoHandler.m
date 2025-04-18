//
//  SLCryptoHandler.m
//  Ellipse
//
//  Created by Andre Green on 8/21/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

#import "SLCryptoHandler.h"
#import <CommonCrypto/CommonHMAC.h>
#import "NSString+Skylock.h"

@implementation SLCryptoHandler

- (NSData *)SHA256WithData:(NSData *)data;
{
    unsigned char hash[CC_SHA256_DIGEST_LENGTH];
    CC_SHA256(data.bytes, (unsigned int)data.length, hash);
    
    return [NSData dataWithBytes:hash length:CC_SHA256_DIGEST_LENGTH];
}

- (NSString *)MD5StringFromString:(NSString *)input {
    return [input MD5String];
}
@end
