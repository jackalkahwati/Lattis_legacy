//
//  Bike+Price.swift
//  Lattis
//
//  Created by Ravil Khusainov on 7/20/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit

extension Bike {
    private var titleTextAttributes: [NSAttributedString.Key: Any] {
        return  [.foregroundColor: UIColor.lsGreyish, .font: UIFont.systemFont(ofSize: 12)]
    }
    
    private var bodyTextAttributes: [NSAttributedString.Key: Any] {
        return  [.foregroundColor: UIColor.lsGreyish, .font: UIFont.systemFont(ofSize: 14)]
    }
    
    private var priceTextAttributes: [NSAttributedString.Key: Any] {
        return [.foregroundColor: UIColor.lsTurquoiseBlue, .font: UIFont.systemFont(ofSize: 14)]
    }
    
    public var infoCost: NSAttributedString {
        guard fleetType == .privatePay || fleetType == .publicPay,
            let price = priceForMembership?.priceValue(currency),
            let duration = priceDuration,
            let units = priceUnit?.localized() else {
                return NSAttributedString(string: "payment_cost_free".localized(), attributes: priceTextAttributes)
        }
        return "payment_bike_details_cost_subtitle".localized().replace(values: [
            String.Replace(source: "#PRICE#", dest: price, attribute: priceTextAttributes),
            String.Replace(source: "#DURATION#", dest: "\(duration)"),
            String.Replace(source: "#UNITS#", dest: units)
            ], style: titleTextAttributes)
    }
    
    public var infoCostText: NSAttributedString {
        if fleetType == .privateFree {
            return NSAttributedString(string: "payment_cost_free_text".localized(), attributes: bodyTextAttributes)
        }
        guard let duration = excessUsageAfterDuration,
            let units = excessUsageAfterUnit?.localized(),
            let fee = excessUsageFees?.priceValue(currency),
            let unit = excessUsageUnit?.localized(),
            let dur = excessUsageDuration
            else {
                return NSAttributedString(string: "", attributes: bodyTextAttributes)
        }
        return "payment_bike_details_cost_text".localized().replace(values: [
            String.Replace(source: "#DURATION#", dest: "\(duration)", attribute: priceTextAttributes),
            String.Replace(source: "#UNITS#", dest: units),
            String.Replace(source: "#PRICE#", dest: fee, attribute: priceTextAttributes),
            String.Replace(source: "#DURATION2#", dest: "\(dur)", attribute: priceTextAttributes),
            String.Replace(source: "#UNIT#", dest: unit, attribute: priceTextAttributes)
            ], style: bodyTextAttributes)
    }
    
    public var infoDeposit: NSAttributedString? {
        guard let deposit = depositPrice?.priceValue(currency) else { return nil }
        let base: String
        switch depositType {
        case .oneTime:
            base = "payment_bike_details_deposit_first_ride"
        case .perRide:
            base = "payment_bike_details_deposit_per_ride"
        default:
            return nil
        }
        return base.localized().replace(values: [
            String.Replace(source: "#PRICE#", dest: deposit, attribute: priceTextAttributes)
            ], style: titleTextAttributes)
    }
    
    public var infoDepositText: NSAttributedString? {
        guard let deposit = depositPrice?.priceValue(currency),
            let duration = refundCriteria,
            let unit = refundCriteriaUnit?.localized() else { return nil }
        return "payment_bike_details_deposit_text".localized().replace(values: [
            String.Replace(source: "#PRICE#", dest: deposit, attribute: priceTextAttributes),
            String.Replace(source: "#DURATION#", dest: "\(duration)", attribute: priceTextAttributes),
            String.Replace(source: "#UNITS#", dest: unit)
            ], style: bodyTextAttributes)
    }
}


extension String {
    struct Replace {
        let source: String
        let dest: String
        let attribute: [NSAttributedString.Key: Any]?
        init(source: String, dest: String, attribute: [NSAttributedString.Key: Any]? = nil) {
            self.source = source
            self.dest = dest
            self.attribute = attribute
        }
    }
    
    struct Attr {
        let attribute: [NSAttributedString.Key: Any]
        let range: NSRange
    }
    
    func replace(values: [Replace], style: [NSAttributedString.Key: Any]) -> NSAttributedString {
        var mutable = self
        var attributes: [Attr] = []
        for value in values {
            if let attribute = value.attribute {
                var range = (mutable as NSString).range(of: value.source)
                range.length = value.dest.count
                attributes.append(Attr(attribute: attribute, range: range))
            }
            mutable = mutable.replacingOccurrences(of: value.source, with: value.dest)
        }
        let attributed = NSMutableAttributedString(string: mutable, attributes: style)
        for attr in attributes {
            attributed.addAttributes(attr.attribute, range: attr.range)
        }
        return attributed
    }
}
