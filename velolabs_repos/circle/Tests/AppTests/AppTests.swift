@testable import App
import XCTVapor
import Fluent
import Vapor

final class AppTests: XCTestCase {
    
    var app: Application!
    let encrypted = "23c798658e928916a4d878a4f354de3731fd028a8c0a1dec2cd8f8b0ebff91ad224a0bf81b8b053cf4bbe4d0132b4fb2f3f7d463806b1d87c861f840d2b4b888"
    let decrypted = "down,right,left,right,down,up,right,up,left,left"
    
    override func setUp() {
        app = Application(.testing)
        do {
            try configure(app)
        } catch {
            XCTAssertThrowsError(error)
        }
    }
    
    override func tearDown() {
        app.shutdown()
    }
    
//    func testGitTag() {
//        guard let tag = Environment.get("GIT_TAG") else { return XCTFail("No GIT_TAG found") }
//        
//        XCTAssertTrue(app.versions.app > tag, "App version should be greater tnan \(tag)")
//    }
    
    func testECDHdecrypt() {
        let expectation = XCTestExpectation(description: "Trying to decrypt")
        do {
            try Lambda.ECDH.decrypt(encrypted, client: app.client)
                .whenComplete { result in
                    switch result {
                    case .failure(let error):
                        XCTAssertThrowsError(error)
                    case .success(let value):
                        print(value)
                        XCTAssert(value == self.decrypted)
                        expectation.fulfill()
                    }
                }
        } catch {
            XCTAssertThrowsError(error)
        }
        wait(for: [expectation], timeout: 5)
    }
    
    func testECDHencrypt() {
        let expectation = XCTestExpectation(description: "Trying to encrypt")
        do {
            try Lambda.ECDH.encrypt(decrypted, client: app.client)
                .whenComplete { result in
                    switch result {
                    case .failure(let error):
                        XCTAssertThrowsError(error)
                    case .success(let value):
                        XCTAssert(value == self.encrypted)
                        expectation.fulfill()
                    }
                }
        } catch {
            XCTAssertThrowsError(error)
        }
        wait(for: [expectation], timeout: 5)
    }
    
    func testEllipse() throws {
        let expectation = XCTestExpectation(description: "Trying to encrypt")
        do {
            try app.test(.GET, "operator/things/ellipse/1096/credentials", headers: .init([("Authorization", "Bearer 7cc84e2eed2862859e06c1cdebfe46322f9bd161333e13b9fef40389469e2df0993887bb215855ca16fc736c8cea25d8")])) { res in
                print(res.body.string, res.status)
                XCTAssert(true)
                expectation.fulfill()
            }
        } catch {
            XCTAssertThrowsError(error)
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 5)
    }
    
    func testKeyExchange() throws {
        let expectation = XCTestExpectation(description: "Trying to encrypt")
        try Lambda.ECDH.publicKey("b8335c9d3b94461c9a66050a0c777d06cdad5904f075846348409b47a576e94d", client: app.client)
            .whenComplete { result in
                switch result {
                case .success(let value):
                    XCTAssert(value == "1ae855009dd7a5c1d69ea1a64f3212d5420ff2f96b75ab2a23f2603410c4411fc7c98506ec2d8922143aca73eb16a36dcfbc0dcfd66fe84889d7dca2050c5901")
                case .failure(let error):
                    XCTAssertThrowsError(error)
                }
                expectation.fulfill()
            }
        wait(for: [expectation], timeout: 5)
    }
    
    func testInverseStatus() throws {
        let expectation = XCTestExpectation(description: "Fetching device status")
        try app.test(.GET, "operator/things/invers/7E000019B9011E01/status", headers: .init([("Authorization", "Bearer 7cc84e2eed2862859e06c1cdebfe46322f9bd161333e13b9fef40389469e2df0993887bb215855ca16fc736c8cea25d8")])) { res in
            XCTAssert(true)
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 5)
    }
    
    func testChangeInverseStatus() throws {
        let expectation = XCTestExpectation(description: "Changing device status")
        let status = InversAPI.Status(central_lock: nil, immobilizer: .unlocked, ignition: nil)
        try app.test(
            .PUT,
            "operator/things/invers/7E000019B9011E01/status",
            headers: .init([("Authorization", "Bearer 7cc84e2eed2862859e06c1cdebfe46322f9bd161333e13b9fef40389469e2df0993887bb215855ca16fc736c8cea25d8")])
        ) { request in
            try request.content.encode(status)
        } afterResponse: { res in
            XCTAssert(true)
            expectation.fulfill()
        }
        wait(for: [expectation], timeout: 5)
    }
    
    func testUpdateToken() async throws {
        let user = try await UserModel.query(on: app.db(.user))
            .filter(\.$id == 74666)
            .first()
            .unwrap(or: Abort(.notFound))
            .get()
        let plain = "zver.kaban@gmail.com*deliminator*\(Int(Date().timeIntervalSince1970) + 28 * 24 * 3600 * 1000)"
        let token = try await Lambda.ECDH.encrypt(plain, client: app.client).get()
        user.restToken = token
        
        try await user.save(on: app.db(.user))
    }
    
    func testDecrypt() async throws {
        let plain = "zver.kaban@gmail.com*deliminator*\(Int(Date().timeIntervalSince1970) + 28 * 24 * 3600 * 1000)"
        let encrypted = "6761cb99b58b6a096617976021c21c55b5243ee73573b459d2e2a7ce6496b24a33bb698f8ed567664ce2952e9d8c4b0d"
        let original = try await Lambda.ECDH.encrypt(plain, client: app.client).get()
        print(original)
        print(plain)
    }
}
