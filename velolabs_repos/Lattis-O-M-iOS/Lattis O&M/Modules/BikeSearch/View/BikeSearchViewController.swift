//
//  BikeSearchBikeSearchViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class BikeSearchViewController: ViewController {
    
    @IBOutlet weak var fakeField: UITextField!
    @IBOutlet var pickerToolbar: UIToolbar!
    @IBOutlet var searchTypeButton: UIButton!
    @IBOutlet weak var textField: TextField!
    @IBOutlet weak var tableView: UITableView!
    
    var interactor: BikeSearchInteractorInput!
    
    fileprivate let searchControl = SearchControl(frame: CGRect(x: 0, y: 0, width: 36, height: 20))
    fileprivate let picker = UIPickerView()
    fileprivate var bikes: [Bike] = []
    fileprivate var searchTypes: [SearchType] = [.bike, .lock]

    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        
        tableView.delegate = self
        tableView.dataSource = self
        tableView.tableFooterView = UIView()
        
        textField.delegate = self
        textField.leftViewMode = .always
        textField.leftView = searchTypeButton
        
        textField.rightViewMode = .always
        textField.rightView = searchControl
        
        searchControl.addTarget(self, action: #selector(clearSearch(_:)), for: .touchUpInside)
        
        fakeField.inputAccessoryView = pickerToolbar
        fakeField.inputView = picker
        picker.delegate = self
        
    }
    
    @objc private func clearSearch(_ sender: SearchControl) {
        guard sender.searchState == .close else { return }
        view.endEditing(true)
        textField.text = ""
        bikes.removeAll()
        tableView.reloadData()
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @IBAction func selectType(_ sender: Any) {
        fakeField.becomeFirstResponder()
    }
    
    @IBAction func pickerDone(_ sender: Any) {
        view.endEditing(true)
    }
    
    @IBAction func textChanged(_ sender: UITextField) {
        searchControl.searchState = sender.text!.isEmpty ? .close : .spin
        interactor.search(by: sender.text!)
    }
}

extension BikeSearchViewController: BikeSearchInteractorOutput {
    func show(bikes: [Bike]) {
        searchControl.searchState = .search
        self.bikes = bikes
        tableView.reloadData()
    }
}

extension BikeSearchViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return bikes.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "bike", for: indexPath) as! BikeSearchCell
        cell.bike = bikes[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        interactor.select(bike: bikes[indexPath.row])
        view.endEditing(true)
    }
}

extension BikeSearchViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return false
    }
}

extension BikeSearchViewController: UIPickerViewDelegate, UIPickerViewDataSource {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return searchTypes.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return searchTypes[row].display
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        let type = searchTypes[row]
        interactor.select(searchType: type)
        searchTypeButton.setImage(type.image, for: .normal)
    }
}

extension SearchType {
    var image: UIImage {
        switch self {
        case .bike:
            return #imageLiteral(resourceName: "icon_bike_small")
        case .member:
            return #imageLiteral(resourceName: "icon_person_small")
        case .lock:
            return #imageLiteral(resourceName: "icon_ellipse")
        }
    }
}
