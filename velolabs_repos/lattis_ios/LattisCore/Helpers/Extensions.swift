//
//  Extensions.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation
import SwiftHEXColors
import Atributika
import SafariServices

extension FileManager {
    func userDirectoryUrl(for userId: Int?) -> URL {
        let urls = self.urls(for: .documentDirectory, in: .userDomainMask)
        let docURL = urls[urls.endIndex-1]
        
        let user = userId == nil ? "shared" : "\(userId!)"
        let url = docURL.appendingPathComponent(user, isDirectory: true)
        if FileManager.default.fileExists(atPath: url.path, isDirectory: nil) == false {
            do {
                try FileManager.default.createDirectory(at: url, withIntermediateDirectories: true, attributes: nil)
            } catch {
                fatalError("\(error)")
            }
        }
        print(url)
        return url
    }
    
    func removeUserDirectory(for userId: Int) {
        let url = userDirectoryUrl(for: userId)
        do {
            try removeItem(at: url)
        } catch {
            print(error)
        }
    }
    
    func userDirectory(for userId: Int?) -> String {
        return userDirectoryUrl(for: userId).path
    }
}


extension String {
    static let loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    
    var isValidEmail: Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailTest.evaluate(with: self)
    }
}

public extension UIViewController {
    static func navigation(_ root: UIViewController, configure: (UINavigationController) -> () = {_ in}) -> UINavigationController {
        let nav = UINavigationController(rootViewController: root)
        nav.navigationBar.prefersLargeTitles = true
        nav.navigationBar.apply(.light)
        configure(nav)
        return nav
    }
}

enum ColorScheme {
    case light
}

extension UINavigationBar {
    func apply(_ style: ColorScheme) {
        switch style {
        case .light:
            tintColor = .black
            barTintColor = .white
            backgroundColor = .white
            isTranslucent = false
            isOpaque = false
            shadowImage = UIImage()
            titleTextAttributes = [.font: UIFont.theme(weight: .bold, size: .title), .foregroundColor: UIColor.black]
            largeTitleTextAttributes = [.font: UIFont.theme(weight: .book, size: .giant), .foregroundColor: UIColor.black]
        }
    }
}

extension UIViewController {
    
    @discardableResult
    func addMenuButton(navigation: Bool) -> UIView? {
        if navigation {
            navigationItem.leftBarButtonItem = .init(image: .named("icon_menu"), style: .plain, target: self, action: #selector(menu))
        } else {
            let button = UIButton.rounded()
            button.setImage(.named("icon_menu"), for: .normal)
            view.addSubview(button)
            constrain(button, view) { btn, view in
                btn.left == view.left + .margin/2
                btn.top == view.safeAreaLayoutGuide.top + .margin/4
            }
            
            button.addTarget(self, action: #selector(menu), for: .touchUpInside)
            return button
        }
        return nil
    }
    
    @discardableResult
    func addCloseButton() -> UIButton? {
        if navigationController == nil {
            let button = UIButton.rounded()
            button.setImage(.named("icon_close"), for: .normal)
            view.addSubview(button)
            constrain(button, view) { close, view in
                close.right == view.right - .margin
                close.top == view.safeAreaLayoutGuide.top + .margin/4
            }
            button.addTarget(self, action: #selector(close), for: .touchUpInside)
            return button
        } else {
            navigationItem.rightBarButtonItem = .init(image: .named("icon_close"), style: .plain, target: self, action: #selector(close))
        }
        return nil
    }
    
    func addBackButton() {
        navigationItem.leftBarButtonItem = .init(image: .named("icon_back"), style: .plain, target: self, action: #selector(back))
    }
    
    @objc func close() {
        dismiss(animated: true)
    }
    
    @objc func back() {
        navigationController?.popViewController(animated: true)
    }
}

extension UIButton {
    
    static func rounded(height: CGFloat = 48, width: CGFloat = 48) -> UIButton {
        let button = UIButton(type: .custom)
        button.backgroundColor = .white
        
        button.tintColor = .black
        button.layer.cornerRadius = min(height, width)/2
        button.addShadow()
        
        constrain(button) { btn in
            btn.height == height
            if !width.isNaN {
                btn.width == width
            }
        }
        
        return button
    }
}

public extension UIImage {
    
    static func named(_ name: String) -> UIImage? {
        let bundle = Bundle(identifier: "io.lattis.LattisCore")
        let image = UIImage(named: name, in: bundle, compatibleWith: nil)
        return image
    }
    
    func resize(to size: CGSize) -> UIImage {
        UIGraphicsBeginImageContext(size)
        self.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage!
    }
}

extension Bundle {
    
    static var core: Bundle? {
        .init(identifier: "io.lattis.LattisCore")
    }
}

fileprivate let theme = UITheme.theme

public extension UIColor {
    static var neonBlue: UIColor {
        return .init(red: 0, green: 170/255, blue: 209/255, alpha: 1)
    }
    
    static var disabledActiveButtonBackgground: UIColor {
        return UIColor(hexString: "E2E2E2")!
    }
        
    static var accentBlue: UIColor { UIColor(red: 47.0/255.0, green: 137.0/255.0, blue: 243.0/255.0, alpha: 1.0) }
    
    static var accentRed: UIColor { UIColor(red: 243.0/255.0, green: 47.0/255.0, blue: 47.0/255.0, alpha: 1.0) }
    
    static var background: UIColor { theme.color.background }
    
    static var secondaryBackground: UIColor { theme.color.secondaryBackground }
    
    static var thirdBackground: UIColor { .init(red: 235.0/255.0, green: 235.0/255.0, blue: 235.0/255.0, alpha: 1) }
    
    static var azureRadiance: UIColor { UIColor(red: 0, green: 0.518, blue: 1, alpha: 1) }
    
    static var dodgerBlue: UIColor { UIColor(red: 48.0/255.0, green: 138.0/255.0, blue: 244.0/255.0, alpha: 1) }
    
    static var accent: UIColor { theme.color.accent }
    
    static var tint: UIColor { theme.color.tint }
    
    static var accentTint: UIColor { theme.color.accentTint }
    
    static var warning: UIColor { theme.color.warning }
    
    static var shadow: UIColor { .init(red: 0, green: 0, blue: 0, alpha: 0.18) }
    
    static var inactiveText: UIColor {
        if #available(iOS 13.0, *) {
            return .systemGray3
        } else {
            return .lightGray
        }
    }
}


extension CGFloat {
    static let margin: CGFloat = 24
    static let containerCornerRadius: CGFloat = 16
}


class CardsFlowLayout: UICollectionViewFlowLayout {
    override func targetContentOffset(forProposedContentOffset proposedContentOffset: CGPoint, withScrollingVelocity velocity: CGPoint) -> CGPoint {
        // Page width used for estimating and calculating paging.
        let pageWidth = self.itemSize.width + self.minimumLineSpacing

        // Make an estimation of the current page position.
        let approximatePage = self.collectionView!.contentOffset.x/pageWidth

        // Determine the current page based on velocity.
        let currentPage = (velocity.x < 0.0) ? floor(approximatePage) : ceil(approximatePage)

        // Create custom flickVelocity.
        let flickVelocity = velocity.x * 0.3

        // Check how many pages the user flicked, if <= 1 then flickedPages should return 0.
        let flickedPages = (abs(round(flickVelocity)) <= 1) ? 0 : round(flickVelocity)

        // Calculate newHorizontalOffset.
        let newHorizontalOffset = ((currentPage + flickedPages) * pageWidth) - self.collectionView!.contentInset.left
        
        let point = CGPoint(x: newHorizontalOffset, y: proposedContentOffset.y)
        
        return point
    }
}

// We are getting the CLLocationCoordinate Equatable conformance from turf.
extension CLLocationCoordinate2D: Equatable {
    static public func ==(lhs: CLLocationCoordinate2D, rhs: CLLocationCoordinate2D) -> Bool {
        lhs.latitude == rhs.latitude && lhs.longitude == rhs.longitude
    }
    
    public var random: CLLocationCoordinate2D {
        let latDif = Double.random(in: -1.01...1.01)
        let lonDif = Double.random(in: -1.01...1.01)
        return .init(latitude: latitude + latDif, longitude: longitude + lonDif)
    }
    
    init(_ la: CLLocationDegrees, _ lo: CLLocationDegrees) {
        self.init(latitude: la, longitude: lo)
    }
}

public extension UIView {
    func startPulse() {
        stopPulse()
        let pulseAnimation = CABasicAnimation(keyPath: #keyPath(CALayer.opacity))
        pulseAnimation.duration = 1
        pulseAnimation.fromValue = 0.5
        pulseAnimation.toValue = 1
        pulseAnimation.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeInEaseOut)
        pulseAnimation.autoreverses = true
        pulseAnimation.repeatCount = .greatestFiniteMagnitude
        
        layer.add(pulseAnimation, forKey: "animateOpacity")
    }
    
    func stopPulse() {
        layer.removeAnimation(forKey: "animateOpacity")
    }
    
    func addShadow(color: UIColor = .black, offcet: CGSize = .init(width: 0, height: 4), radius: CGFloat = 5, opacity: Float = 0.11) {
        layer.shadowColor = color.cgColor
        layer.shadowOffset = offcet
        layer.shadowRadius = radius
        layer.shadowOpacity = opacity
    }
}

extension UILabel {
    func headerStyle() {
        textAlignment = .center
        font = .theme(weight: .medium, size: .body)
        textColor = .lightGray
        numberOfLines = 0
    }
}

extension TimeInterval {
    static let minute: TimeInterval = 60
    static let hour: TimeInterval = minute * 60
    static let day: TimeInterval = hour * 24
    static let month: TimeInterval = day * 30
    
    var minutes: TimeInterval { self * .minute }
    var hours: TimeInterval { self * .hour }
    var days: TimeInterval { self * .day }
    var months: TimeInterval { self * .month }
}

extension NumberFormatter {
    static let price: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        return formatter
    }()
}

extension Double {
    func price(for currency: String?) -> String? {
        let num = NSNumber(value: self)
        let formatter = NumberFormatter.price
        formatter.currencyCode = currency ?? "USD"
        formatter.minimumFractionDigits = modf(self).1 > 0 ? 2 : 0
        formatter.maximumFractionDigits = 2
        formatter.minimumIntegerDigits = 1
        return formatter.string(from: num)
    }
}

extension Int {
    static let codeLimit: Int = 6
    static let passwordMax: Int = 16
    static let passwordMin: Int = 8
}

extension AttributedLabel {
    static func legal(_ parent: UIViewController, text: String? = nil) -> AttributedLabel {
        let label = AttributedLabel()
        label.numberOfLines = 0
        label.textAlignment = .center
        let all = Style.font(.theme(weight: .light, size: .small))
            .foregroundColor(.black)
        let link = Style("a")
            .foregroundColor(.black, .normal)
            .foregroundColor(.blue, .highlighted)
            .underlineStyle(.single)
        label.attributedText = (text ?? UITheme.theme.legal)
            .style(tags: link)
            .styleLinks(link)
            .styleAll(all)
        label.onClick = { [weak parent] label, detection in
            guard case let .tag(tag) = detection.type,
                let url = tag.url else { return }
            let safari = SFSafariViewController(url: url)
            parent?.present(safari, animated: true, completion: nil)
        }
        return label
    }
}

extension Tag {
    var url: URL? {
        guard name == "a", let href = attributes["href"] else { return nil }
        return URL(string: href)
    }
}

extension Equatable {
    func `in`(_ array: [Self]) -> Bool {
        array.contains(self)
    }
}

infix operator ~~ 
func ~~<Item: Equatable>(item: Item, array: [Item]) -> Bool {
    item.in(array)
}

