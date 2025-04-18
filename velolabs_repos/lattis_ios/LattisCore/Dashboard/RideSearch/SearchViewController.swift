//
//  LocationSearchViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 04/06/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import MapboxGeocoder
import CoreLocation

enum SearchResult {
    case current
    case address(Address)
    case vehicle(Bike)
}

protocol SearchItem {
    var title: String? { get }
    var subtitle: String? { get }
    var iconName: String { get }
}

class SearchViewController: UIViewController {
    
    fileprivate let topView = UIView()
    fileprivate let textField = UITextField()
    fileprivate let tableView = UITableView()
    
    fileprivate let didSelectAddress: (SearchResult) -> ()
    fileprivate var initialAddress: Address?
    fileprivate let location: CLLocation?
    fileprivate let storage = JSONStorage<[Address]>("addresses.json")
    
    fileprivate var initialConstraint: NSLayoutConstraint!
    fileprivate var animationConstraint: NSLayoutConstraint!
    fileprivate var bottomConstraint: NSLayoutConstraint!
    
    fileprivate var recent: [Address] = [
//        .init(name: "Bakhetly", coordinate: .init(55.838199, 49.082787), street: "17A Dekabristov st.", city: "Kazan", country: "Russia"),
//        .init(name: "Lattis HQ", coordinate: .init(55.838199, 49.082787), street: "661 Natoma st.", city: "San-Francisco", country: "United States"),
//        .init(name: "Home", coordinate: .init(55.838199, 49.082787), street: "23 Chernishevsky st.", city: "Tenishevo", country: "Russia"),
//        .init(name: "Lake", coordinate: .init(55.838199, 49.082787), street: "456 Quaquille st.", city: "Annecy", country: "France")
    ]
    fileprivate var searchResults: [Address] = []
    fileprivate var vehicles: [Bike] = []
    fileprivate var results: [SearchItem] = []
    fileprivate let api: HubsAPI = AppRouter.shared.api()
    fileprivate var task: URLSessionTask?
    
    init(address: Address?, location: CLLocation?, callback: @escaping (SearchResult) -> ()) {
        self.didSelectAddress = callback
        self.initialAddress = address
        self.location = location
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        Analytics.log(.search)
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = .white
        
        let closeButton = ActionButton(.plain(title: "close".localized(), style: .plain, handler: { [unowned self] in self.hide() }))
        closeButton.setTitleColor(.black, for: .normal)
        closeButton.tintColor = .black
        
        let searchImage = UIImageView(image: .named("icon_search"))
        searchImage.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        topView.addSubview(searchImage)
        
        view.addSubview(topView)        
        topView.addSubview(textField)
        topView.addSubview(closeButton)
        textField.textColor = .black
        textField.font = .theme(weight: .medium, size: .body)
        textField.attributedPlaceholder = NSAttributedString(string: "label_enter_location".localized(), attributes: [.font: UIFont.theme(weight: .medium, size: .body), .foregroundColor: UIColor.lightGray])
        textField.text = initialAddress?.name
        textField.addTarget(self, action: #selector(onTextChange(_:)), for: .editingChanged)
        
        view.addSubview(tableView)
        tableView.tableFooterView = UIView()
        tableView.register(SearchItemCell.self, forCellReuseIdentifier: "cell")
        tableView.dataSource = self
        tableView.delegate = self
        tableView.clipsToBounds = true
        tableView.layer.cornerRadius = .margin/2
        tableView.rowHeight = UITableView.automaticDimension
        tableView.estimatedRowHeight = 55
        tableView.sectionHeaderHeight = 44
        tableView.separatorStyle = .none
        
        constrain(closeButton, topView, tableView, view) { close, top, table, view in
            top.top == view.safeAreaLayoutGuide.top + .margin/4
            top.left == view.left + .margin
            top.right == view.right - .margin
            top.height == 48
            
            table.left == view.left
            table.right == view.right
            self.animationConstraint = table.top == top.bottom + .margin/2 ~ .defaultLow
            self.initialConstraint = table.top == view.bottom ~ .defaultHigh
            table.height >= 100
            self.bottomConstraint = table.bottom == view.bottom ~ .defaultLow
        }
        
        constrain(textField, closeButton, searchImage, topView) { text, close, search, top in
            text.centerY == top.centerY
            text.left == search.right + .margin/2
            text.right == close.left - .margin/2
            
            close.top == top.top
            close.right == top.right
            close.bottom == top.bottom
            
            search.left == top.left
            search.centerY == top.centerY
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(willShowKeybaard(notification:)), name: UIResponder.keyboardWillShowNotification, object: nil)
        storage.fetch { (recent) in
            self.recent = recent
            calculate()
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        textField.becomeFirstResponder()
        
        guard initialConstraint.priority != .defaultLow else { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.initialConstraint.priority = .defaultLow
            self.animationConstraint.priority = .defaultHigh
            UIView.animate(withDuration: 0.2, delay: 0, options: .curveEaseIn, animations: self.view.layoutIfNeeded, completion: nil)
        }
    }
    
    @objc fileprivate func onTextChange(_ sender: UITextField) {
        guard sender.text != nil else { return }
        search()
    }
    
    @objc fileprivate func hide() {
        textField.resignFirstResponder()
        initialConstraint.priority = .defaultHigh
        animationConstraint.priority = .defaultLow
        bottomConstraint.priority = .defaultLow
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseOut, animations: view.layoutIfNeeded, completion: nil)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2, execute: close)
    }
    
    @objc fileprivate func currentLocation() {
        didSelectAddress(.current)
        hide()
    }
    
    @objc fileprivate func willShowKeybaard(notification: Notification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            bottomConstraint.constant = -keyboardSize.height
            bottomConstraint.priority = .defaultHigh
        }
    }
    
    fileprivate func calculate() {
        results = searchResults.isEmpty ? recent : searchResults
        results += vehicles
        tableView.reloadData()
    }
    
    fileprivate func search() {
        guard let text = textField.text, !text.isEmpty else {
            searchResults.removeAll()
            calculate()
            return
        }
        
        let options = ForwardGeocodeOptions(query: text)
        options.focalLocation = location
        options.maximumResultCount = 10
        Geocoder.shared.geocode(options) { [weak self] (placemarks, _, error) in
            self?.fetch(placemarks: placemarks, error: error)
        }
        
        task?.cancel()
        task = api.filter(bikeName: text) { [weak self] results in
            guard self?.textField.text == text else { return }
            switch results {
            case .failure(let error):
                Analytics.report(error)
            case .success(let bikes):
                self?.vehicles = bikes
                self?.calculate()
            }
        }
    }
    
    fileprivate func fetch(placemarks: [GeocodedPlacemark]?, error: Error?) {
        guard let pl = placemarks else { return }
        searchResults = pl.compactMap(Address.init)
        DispatchQueue.main.async(execute: calculate)
    }
}

extension SearchViewController: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return results.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! SearchItemCell
        cell.item = results[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        if !recent.isEmpty && searchResults.isEmpty {
            return PlainTextSectionHeader("Recent")
        }
        return nil
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        if !recent.isEmpty && searchResults.isEmpty {
            return 44
        }
        return 0
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let address = results[indexPath.row] as? Address {
            storage.save(address)
            textField.text = address.name
            didSelectAddress(.address(address))
        }
        if let bike = results[indexPath.row] as? Bike {
            didSelectAddress(.vehicle(bike))
        }
        hide()
    }
}

extension Address: SearchItem {
    var title: String? {
        var title = name
        if let street = street, !street.isEmpty {
            title += ", \(street)"
        }
        return title
    }
    
    var subtitle: String? {
        if let city = city, !city.isEmpty {
            var subtitle = city
            if let country = country {
                subtitle += ", \(country)"
            }
            return subtitle
        } else if let country = country, !country.isEmpty {
            return country
        } else {
            return nil
        }
    }
    var iconName: String { "icon_location_search_result" }
}

extension Bike: SearchItem {
    var iconName: String { identifier }
}
