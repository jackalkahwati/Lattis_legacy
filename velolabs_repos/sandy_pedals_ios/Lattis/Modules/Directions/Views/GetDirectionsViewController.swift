//
//  GetDirectionsViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 13/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Localize_Swift
import GooglePlaces
import GoogleMapsBase

class GetDirectionsViewController: ViewController {
    @IBOutlet weak var directionsView: GetDirectionsView!
    var interactor: DirectionsInteractorInput!
    var currentAllowed: Bool = true
    fileprivate var recient: [Direction] = []
    fileprivate var searchDirections: [Direction] = []
    fileprivate var directions: [Direction] {
        return isSearch ? searchDirections : recient
    }
    fileprivate var isSearch: Bool = false

    fileprivate var searchOperation: BlockOperation?
    fileprivate var placesClient: GMSPlacesClient!
    fileprivate var userLocation: CLLocation?
    fileprivate let locationManager = CLLocationManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if title == nil{
            title = "directions_title".localized()
        } else {
            directionsView.searchField.placeholder = "directions_pick_up_placeholder".localized()
        }
        if currentAllowed == false {
            directionsView.currentHeight.constant = 0
        }
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_:)))
        navigationController?.isNavigationBarHidden = false
        
        directionsView.tableView.dataSource = self
        directionsView.tableView.delegate = self
        
        directionsView.searchField.delegate = self
        
        directionsView.searchControl.addTarget(self, action: #selector(clearSearch(_:)), for: .touchUpInside)
        
        locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            locationManager.startUpdatingLocation()
        }
        GMSPlacesClient.provideAPIKey("AIzaSyBZa6MS_pgGw8821T7q76gXkezf2UiOPso")
        placesClient = .shared()
        interactor.getRecientDirections()
    }
    
    @objc private func close(_ sender: Any) {
        navigationController?.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func textChanged(_ sender: UITextField) {
        directionsView.searchControl.searchState = sender.text!.isEmpty ? .close : .spin
        searchOperation?.cancel()
        searchOperation = BlockOperation(block: {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5, execute: { [weak self] in self?.search() })
        })
        searchOperation?.start()
    }
    
    @objc private func clearSearch(_ sender: GetDirectionsView.SearchControl) {
        guard sender.searchState == .close else { return }
        view.endEditing(true)
        directionsView.searchField.text = ""
        reload(directions: [], isReceint: true)
    }
    
    fileprivate func reload(directions: [Direction], isReceint: Bool = false) {
        DispatchQueue.main.async {
            self.directionsView.searchControl.searchState = self.directionsView.searchField.text!.isEmpty ? .search : .close
            if isReceint {
                self.recient = directions
            } else {
                self.searchDirections = directions
            }
            self.isSearch = !isReceint
            self.directionsView.tableView.reloadData()
            if directions.isEmpty {
                self.directionsView.showEmpty()
            } else {
                self.directionsView.hideEmpty()
            }
        }
    }
    
    private func search() {
        guard let text = directionsView.searchField.text, text.isEmpty == false else { return reload(directions: [], isReceint: true) }
        let token = GMSAutocompleteSessionToken()
        let filter = GMSAutocompleteFilter()
        filter.type = .noFilter
        placesClient.findAutocompletePredictions(
            fromQuery: text,
            bounds: nil,
            boundsMode: .bias,
            filter: filter,
            sessionToken: token,
            callback: { [weak self] (results, error) in
                guard let places = results else {
                    self?.reload(directions: [], isReceint: false)
                    return
                }
                self?.reload(directions: places.map(Direction.init), isReceint: false)
        })
    }
    
    @IBAction func currentLocation(_ sender: Any) {
        interactor.selectCurrentLocation()
    }
}

extension GetDirectionsViewController: DirectionsInteractorOutput {
    func show(directions: [Direction]) {
        reload(directions: directions, isReceint: true)
    }
}

extension GetDirectionsViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return directions.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String(describing: GetDirectionsCell.self), for: indexPath) as! GetDirectionsCell
        cell.direction = directions[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return isSearch ? 0 : 55
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        return isSearch ? nil : GetDirectionsSectionView(title: "directions_recent_directions".localized())
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let direction = directions[indexPath.row]
        if let place = direction.placeId {
            startLoading()
            placesClient.fetchPlace(fromPlaceID: place, placeFields: .all, sessionToken: nil) { [weak self] (place, error) in
                if let error = error {
                    self?.show(error: error, file: #file, line: #line)
                } else if let place = place {
                    self?.stopLoading(completion: {
                        self?.interactor.select(.init(place))
                    })
                }
            }
        } else {
            interactor.select(direction)
        }
    }
}

extension GetDirectionsViewController: UITextFieldDelegate {
    
}

extension GetDirectionsViewController: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        userLocation = manager.location
    }
}

extension Direction {
    init(_ prediction: GMSAutocompletePrediction) {
        name = prediction.attributedPrimaryText.string
        address = prediction.attributedSecondaryText?.string
        placeId = prediction.placeID
        rating = 0
    }
    
    init(_ place: GMSPlace) {
        name = place.name
        address = place.formattedAddress
        coordinate = place.coordinate
        placeId = nil
        rating = 0
    }
}
