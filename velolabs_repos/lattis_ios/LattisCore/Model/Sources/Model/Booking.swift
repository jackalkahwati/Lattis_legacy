

import Foundation


public struct Booking: Codable {
    public init(bookingId: Int, supportPhone: String, onCallOperator: String?, bookedOn: Date, expiresIn: TimeInterval) {
        self.bookingId = bookingId
        self.supportPhone = supportPhone
        self.onCallOperator = onCallOperator
        self.bookedOn = bookedOn
        self.expiresIn = expiresIn
    }
    
    public let bookingId: Int
    public let supportPhone: String
    public let onCallOperator: String?
    public let bookedOn: Date
    public let expiresIn: TimeInterval
    
    public var deadline: Date { bookedOn.addingTimeInterval(expiresIn) }
}
