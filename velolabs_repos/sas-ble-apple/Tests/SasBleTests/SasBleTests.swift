import XCTest
@testable import SasBle

final class SasBleTests: XCTestCase {
    
    override func setUp() {
//        SasAPI.token = .init(token: "eyJraWQiOiJJNXhKZUdaaEpPWDQ5bFYyYTZtUjBRRER0UDFTWFpvRGp1NFZNc3FVd3JVPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI1M2ZrZGNhZWN0YzloNHRwZ2tuZGQxcjZtIiwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJwdWJsaWMtYXBpXC9wdWJsaWMtYXBpIiwiYXV0aF90aW1lIjoxNjQ1Njg4MzIzLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAuZXUtY2VudHJhbC0xLmFtYXpvbmF3cy5jb21cL2V1LWNlbnRyYWwtMV9nbFhtZGxYak8iLCJleHAiOjE2NDU2OTE5MjMsImlhdCI6MTY0NTY4ODMyMywidmVyc2lvbiI6MiwianRpIjoiZGE5ZjE2YzQtNjkyMy00MGRhLTgwNjctODBhZmEzOTIxNDEwIiwiY2xpZW50X2lkIjoiNTNma2RjYWVjdGM5aDR0cGdrbmRkMXI2bSJ9.VeM6vzd3ZxdNGBnWbNBWo3vlAssD0Ba2TLMeDPurIuqJofhZkKyXRoaSGLIdVT-24WXCKOHE-wsIjkNKjgcB4SVEVaXc0e_vqIJD-THvwnfzafls4zW8qzaxHtGZZKxzCxXy28qaDSe7m9ngm5GLkAxrPbXrioa4C46Fiqn3fT7kHPl9Hb8DIU70xKHYHL8DwsofRRg3DfI8bLgW5ErdqB_rJJRsTofuVXhXtZc79xx_mez2hJGJFHV69JKdfH1B7OH1LT8Ehu33eOoFS9vz6E9DxLlSfp8XUNDq-Cu3zz9F0roZkc2xFENnBAx13cEMZVCJ_SV9vebeVdRSKGD1_g", date: Date().addingTimeInterval(9000))
    }
    
    func testBytesToHex() {
        let str = Data([UInt8(0x36)]).hexEncodedString()
        print(str)
        XCTAssert(false)
    }
    
}

