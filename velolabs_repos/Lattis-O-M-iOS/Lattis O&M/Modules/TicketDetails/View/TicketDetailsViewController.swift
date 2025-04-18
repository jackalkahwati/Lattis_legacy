//
//  TicketDetailsTicketDetailsViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 27/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import SDWebImage
import Mapbox

class TicketDetailsViewController: ViewController {
    
    @IBOutlet var pickerToolbar: UIToolbar!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var subtitleLabel: UILabel!
    @IBOutlet weak var address1Label: UILabel!
    @IBOutlet weak var address2Label: UILabel!
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var dateLavbel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var notesLabel: UILabel!
    @IBOutlet weak var photoView: UIImageView!
    @IBOutlet weak var assignedField: TextField!
    @IBOutlet weak var mapView: MGLMapView!
    
    var interactor: TicketDetailsInteractorInput!
    
    fileprivate let assigneePicker = UIPickerView()
    fileprivate var operators: [Operator?] = []
    fileprivate let timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        formatter.dateStyle = .none
        return formatter
    }()
    fileprivate let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.timeStyle = .none
        formatter.dateStyle = .medium
        return formatter
    }()
    fileprivate var isClose: Bool = false
    override func viewDidLoad() {
        super.viewDidLoad()

        navigationItem.leftBarButtonItem = .back(target: self, action: #selector(back))
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "ticket_details_resolve".localized().uppercased(), style: .plain, target: self, action: #selector(resolve))
        
        mapView.delegate = self
        assigneePicker.dataSource = self
        assigneePicker.delegate = self
        
        assignedField.inputView = assigneePicker
        assignedField.inputAccessoryView = pickerToolbar
        
        interactor.viewLoaded()
    }
    
    @objc private func back() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    @objc func resolve() {
        interactor.resolve()
    }
    
    @IBAction func getDirection(_ sender: Any) {
        interactor.getDirection(userCoordinate: mapView.userLocation?.coordinate ?? kCLLocationCoordinate2DInvalid)
    }
    
    @IBAction func pickerDone(_ sender: Any) {
        view.endEditing(true)
        let oper = operators[assigneePicker.selectedRow(inComponent: 0)]
        interactor.assign(oper: oper)
    }
}

extension TicketDetailsViewController: TicketDetailsInteractorOutput {
    func show(ticket: Ticket) {
        titleLabel.text = ticket.displayTitle
        subtitleLabel.text = ticket.category?.displayTitle
        let notes = ticket.riderNotes ?? ticket.operatorNotes
        notesLabel.text = notes ?? ticket.maintenanceNotes
        timeLabel.text = timeFormatter.string(from: ticket.created)
        dateLavbel.text = dateFormatter.string(from: ticket.created)
        let photo = ticket.userPhoto ?? ticket.operatorPhoto
        if let url = photo {
            photoView.sd_setImage(with: url, placeholderImage: #imageLiteral(resourceName: "placeholder-image"))
        }
        if CLLocationCoordinate2DIsValid(ticket.coordinate) {
            let annotation = Annotation(coordinate: ticket.coordinate)
            mapView.addAnnotation(annotation)
        }
        statusLabel.text = ticket.displayStatus
    }
    
    func show(operators: [Operator], selected: Int, unassigned: Bool) {
        self.operators = unassigned ? [nil] + operators : operators
        self.assigneePicker.selectRow(selected, inComponent: 0, animated: false)
        assigneePicker.reloadAllComponents()
        assignedField.text = self.operators[selected]?.fullName ?? "ticket_details_unassigned".localized()
    }
    
    func show(address topLine: String, bottomLine: String?) {
        address1Label.text = topLine
        address2Label.text = bottomLine
    }
}

extension TicketDetailsViewController: UIPickerViewDataSource, UIPickerViewDelegate {
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return operators.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return operators[row]?.fullName ?? "ticket_details_unassigned".localized()
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        assignedField.text = operators[row]?.fullName ?? "ticket_details_unassigned".localized()
    }
}

extension TicketDetailsViewController: MGLMapViewDelegate {
    func mapView(_ mapView: MGLMapView, imageFor annotation: MGLAnnotation) -> MGLAnnotationImage? {
        let identifire = "identifire"
        var image = mapView.dequeueReusableAnnotationImage(withIdentifier: identifire)
        if image == nil {
            var img = #imageLiteral(resourceName: "icon_bike_red")
            img = img.withAlignmentRectInsets(UIEdgeInsets(top: 0, left: 0, bottom: img.size.height, right: 0))
            image = MGLAnnotationImage(image: img, reuseIdentifier: identifire)
        }
        return image
    }
    
    func mapView(_ mapView: MGLMapView, didUpdate userLocation: MGLUserLocation?) {
        guard var points = mapView.annotations?.map({ $0.coordinate }),
            let user = userLocation?.coordinate,
            isClose == false else { return }
        isClose = true
        points.append(user)
        var sw = kCLLocationCoordinate2DInvalid
        var ne = kCLLocationCoordinate2DInvalid
        sw.latitude = points.map({ $0.latitude }).max()! + 0.0000001//37.791905229074075
        sw.longitude = points.map({ $0.longitude }).min()! - 0.0000001 //-122.48287349939346
        ne.latitude = points.map({ $0.latitude }).min()! + 0.0000001//37.74427534910713
        ne.longitude = points.map({ $0.longitude }).max()! - 0.0000001//-122.39103466272354
        let bounds = MGLCoordinateBounds(sw: sw, ne: ne)
        let camera = mapView.cameraThatFitsCoordinateBounds(bounds)
        mapView.setCamera(camera, animated: true)
    }
}

class TextField: UITextField {
    override func textRect(forBounds bounds: CGRect) -> CGRect {
        return super.textRect(forBounds: bounds).insetBy(dx: 8, dy: 0)
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        return super.editingRect(forBounds: bounds).insetBy(dx: 8, dy: 0)
    }
}

class Annotation: NSObject, MGLAnnotation {
    var coordinate: CLLocationCoordinate2D
    var title: String?
    var subtitle: String?
    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
        super.init()
    }
}

