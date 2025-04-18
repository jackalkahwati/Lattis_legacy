//
//  SLCryptoHandler.h
//  Ellipse
//
//  Created by Andre Green on 8/21/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SLCryptoHandler : NSObject

- (NSData *)SHA256WithData:(NSData *)data;
- (NSString *)MD5StringFromString:(NSString *)input;

@end
