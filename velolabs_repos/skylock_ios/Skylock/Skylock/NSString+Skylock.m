//
//  NSString+Skylock.m
//  Skylock
//
//  Created by Andre Green on 6/19/15.
//  Copyright (c) 2015 Andre Green. All rights reserved.
//

#import "NSString+Skylock.h"
#import "SLConstants.h"
#import <CommonCrypto/CommonDigest.h>

@implementation NSString (Skylock)

- (instancetype)stringWithDistance:(NSNumber *)distance
{
    NSLog(@"%@ %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd));
    // should figure out a way to do this that does not involve hard coding the distance unit
    // probably should be a server side fix, or an option that the user can configure
    if (distance.integerValue < SLConstantsFeetInMile) {
        return [NSString stringWithFormat:@"%@ft away", distance];
    } else {
        float miles = distance.floatValue/(float)SLConstantsFeetInMile;
        return [NSString stringWithFormat:@"%.1fmi away", miles];
    }
}

- (CGSize)sizeWithFont:(UIFont *)font maxSize:(CGSize)maxSize
{
    CGRect rect = [self boundingRectWithSize:maxSize
                                     options:NSStringDrawingUsesLineFragmentOrigin
                                  attributes:@{NSFontAttributeName:font}
                                     context:nil];
    return rect.size;
}

- (NSString *)MD5String {
    const char *cstr = [self UTF8String];
    unsigned char result[16];
    CC_MD5(cstr, strlen(cstr), result);
    
    return [NSString stringWithFormat:
            @"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
            result[0], result[1], result[2], result[3],
            result[4], result[5], result[6], result[7],
            result[8], result[9], result[10], result[11],
            result[12], result[13], result[14], result[15]
            ];  
}

- (NSData *)bytesString
{
    Byte bytes[self.length/2];
    
    for (int i=0; i < self.length; i += 2) {
        NSString *hexValue = [self substringWithRange:NSMakeRange(i, 2)];
        unsigned int decVal = 0 ;
        NSScanner* scan = [NSScanner scannerWithString:hexValue];
        [scan scanHexInt:&decVal];
        scan = nil;
        
        Byte byteValue = decVal;
        bytes[i/2] = byteValue;
    }
    
    return [NSData dataWithBytes:&bytes length:self.length/2];
}

- (NSString *)macAddress
{
    NSArray *parts;
    if ([self rangeOfString:@"-"].location == NSNotFound &&
        [self rangeOfString:@" "].location != NSNotFound) {
        parts = [self componentsSeparatedByString:@" "];
        return parts[1];
    }
    
    if ([self rangeOfString:@" "].location == NSNotFound &&
        [self rangeOfString:@"-"].location != NSNotFound) {
        parts = [self componentsSeparatedByString:@"-"];
        return parts[1];
    }
    
    return nil;
}

@end
