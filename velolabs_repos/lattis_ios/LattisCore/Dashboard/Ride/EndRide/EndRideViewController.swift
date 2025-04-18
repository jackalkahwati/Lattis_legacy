//
//  EndRideViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 06/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import OvalAPI

class EndRideViewController: UIViewController {
    
    fileprivate let photoButton = ActionButton()
    
    fileprivate var endInfo: Trip.End
    fileprivate var tripService: TripManager
    fileprivate let callback: () -> ()
    fileprivate let network: TripAPI & FileNetwork = AppRouter.shared.api()
    
    init(_ endInfo: Trip.End,
         tripService: TripManager,
         callback: @escaping () -> ()) {
        self.endInfo = endInfo
        self.callback = callback
        self.tripService = tripService
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        title = "end_ride".localized()
        addCloseButton()
        view.backgroundColor = .white
        view.addSubview(photoButton)
        photoButton.action = .plain(title: nil, icon: .named("icon_end_ride_photo"), handler: { [unowned self] in
            self.takePicture()
        })
        
        let checklist = UILabel.label(text: "end_ride_sub_title".localized(), font: .theme(weight: .light, size: .body), color: .darkGray, allignment: .center, lines: 0)
        view.addSubview(checklist)

        let stack = UIStackView()
        stack.axis = .vertical
        stack.distribution = .fillEqually
        stack.spacing = .margin*2
        let scrollView = stack.addScroll(insets: .init(top: 0, left: .margin, bottom: 0, right: .margin), to: view)
        
        constrain(photoButton, scrollView, checklist, view) { button, st, cl, view in
            button.bottom == view.safeAreaLayoutGuide.bottom - .margin
            button.left == view.left + .margin
            button.right == view.right - .margin
            cl.top == view.safeAreaLayoutGuide.top + .margin
            cl.left == view.left + .margin
            cl.right == view.right - .margin
            
            st.top == cl.bottom + .margin
            st.left == view.left
            st.right == view.right
            st.bottom == button.top - .margin
        }
        
        stack.addArrangedSubview(
            listItem(
                title: "end_ride_parking_title_authorized".localized(),
                body: "end_ride_parking_sub_title_authorized".localized(),
                icon: .named("icon_end_ride_parking")
            )
        )
        stack.addArrangedSubview(
            listItem(
                title: "end_ride_locked_title".localized(),
                body: "end_ride_locked_sub_title".localized(),
                icon: .named("icon_end_ride_lock")
            )
        )
        stack.addArrangedSubview(
            listItem(
                title: "end_ride_photo_title".localized(),
                body: "end_ride_photo_sub_title".localized(),
                icon: .named("icon_end_ride_photo")
            )
        )
        
        NotificationCenter.default.addObserver(self, selector: #selector(handle(notification:)), name: .tripUpdated, object: nil)
    }
    
    fileprivate func listItem(title: String, body: String, icon: UIImage?) -> UIView {
        let titleLabel = UILabel.label(text: title, font: .theme(weight: .medium, size: .body), color: .black, lines: 2)
        
        let bodyLabel = UILabel.label(text: body, font: .theme(weight: .book, size: .text), color: .gray, lines: 0)
        
        let imageView = UIImageView(image: icon)
        imageView.contentMode = .center
        imageView.backgroundColor = .accent
        imageView.tintColor = .tint
        imageView.layer.cornerRadius = 10
        imageView.clipsToBounds = true
        
        let container = UIView()
        container.addSubview(imageView)
        container.addSubview(bodyLabel)
        container.addSubview(titleLabel)
        
        constrain(titleLabel, bodyLabel, imageView, container) { title, body, image, view in
            title.top == view.top
            title.left == view.left
            title.right == image.left - .margin
            
            body.top == title.bottom + .margin/2
            body.left == view.left
            body.right == title.right
            body.bottom >= view.bottom
            body.right >= image.left - .margin
            
            image.right == view.right
            image.height == 64
            image.width == image.height
            image.centerY == view.centerY
            image.top >= view.top
            image.bottom <= view.bottom
        }
        return container
    }
    
    @objc
    fileprivate func takePicture() {
        let pick = UIImagePickerController()
        #if TARGET_OS_SIMULATOR
        pick.sourceType = .photoLibrary
        #else
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            pick.sourceType = .camera
        }
        #endif
        pick.delegate = self
        pick.allowsEditing = true
        present(pick, animated: true, completion: nil)
    }
    
    fileprivate func endTrip(image: URL) {
        endInfo.parkingImage = image
        _ = tripService.endTrip(endInfo, parking: false)
    }
    
    @objc
    fileprivate func handle(notification: Notification) {
        guard let state = notification.object as? TripManager.Status else { return }
        switch state {
        case .failure(let error):
            handle(error)
        case .finished(let trip, _):
            tripSummary(trip: trip)
        default:
            break
        }
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        self.dismiss(animated: true) {
            if let err = error as? SessionError, err.code == .conflict {
                self.handlePaymentError()
            } else {
                super.handle(error, from: viewController, retryHandler: retryHandler)
            }
        }
    }
    
    fileprivate func handlePaymentError() {
        present(AlertController.cardRequired(completion: openPaymentMethods), animated: true, completion: nil)
    }
    
    fileprivate func openPaymentMethods() {
        navigationController?.pushViewController(PaymentMethodsViewController(logic: .init(bike: tripService.bike)), animated: true)
    }
    
    fileprivate func upload(image: UIImage) {
        guard let data = image.jpegData(compressionQuality: 0.5) else { return }
        network.upload(data: data, for: .parking) { [weak self] (result) in
            switch result {
            case .success(let url):
                self?.endTrip(image: url)
            case .failure(let error):
                self?.handle(error, retryHandler: {
                    self?.dismiss(animated: true, completion: nil)
                })
            }
        }
    }
    
    fileprivate func tripSummary(trip: Trip) {
        dismiss(animated: true, completion: nil)
        let sum = TripSummaryViewController(trip, callback: callback)
        navigationController?.pushViewController(sum, animated: false)
    }
}

extension EndRideViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        picker.pushViewController(ActivityViewController("end_ride_loader".localized()), animated: true)
        guard let image = info[.editedImage] as? UIImage else { return }
        upload(image: image.resize(to: .init(width: 1024, height: 1024)))
    }
}
