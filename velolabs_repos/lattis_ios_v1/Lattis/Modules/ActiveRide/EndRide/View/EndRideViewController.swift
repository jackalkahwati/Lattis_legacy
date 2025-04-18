//
//  EndMyRideViewController.swift
//  Lattis
//
//  Created by Ravil Khusainov on 17/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import Oval

class EndRideViewController: ViewController {
    @IBOutlet weak var nextButton: UIButton!
    var interactor: EndRideInteractorInput!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        title = "end_trip_title".localized()
        navigationItem.leftBarButtonItem = .close(target: self, action: #selector(close(_:)))
        navigationController?.isNavigationBarHidden = false
    }
    

    @IBAction func close(_ sender: Any) {
        AppRouter.shared.endTrip = {_ in}
        AppRouter.shared.tripEnded = nil
        navigationController?.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func confirmAction(_ sender: Any) {
        let picker = UIImagePickerController()
        picker.sourceType = TARGET_OS_SIMULATOR != 0 ? .photoLibrary : .camera
        picker.delegate = self
        present(picker, animated: true, completion: nil)
    }
}

extension EndRideViewController: EndRideInteractorOutput {
    func show(snapshot: UIImage) {}

    func show(trip: Trip){}
    
    override func show(error: Error, file: String, line: Int) {
        if let err = error as? SessionError {
            switch err.code {
            case .lengthRequired, .badRequest:
                warning(with: "public_fleet_no_stripe_title".localized(), subtitle: "public_fleet_no_stripe_text".localized())
            case .conflict:
                let alert = ActionAlertView.alert(title: "end_ride_stripe_error_title".localized(), subtitle: "end_ride_stripe_error_text".localized())
                alert.action = AlertAction(title: "end_ride_stripe_error_action".localized(), action: interactor.openPayments)
                alert.cancel = AlertAction(title: "end_ride_stripe_error_cancel".localized(), action: interactor.dismiss)
                stopLoading {
                    alert.show()
                }
            default:
                break
            }
            Analytics.report(error, file: file, line: line)
            
        } else {
            super.show(error: error, file: file, line: line)
        }
    }
}

extension EndRideViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true) { 
            
        }
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[.originalImage] as? UIImage else { return }
        picker.dismiss(animated: true) {
            self.interactor.didMake(picture: image.resizeImageWith(newSize: .side(800)))
        }
    }
}


extension CGSize {
    static func side(_ side: CGFloat) -> CGSize {
        return CGSize(width: side, height: side)
    }
}

extension UIImage{
    func resizeImageWith(newSize: CGSize) -> UIImage {
        let horizontalRatio = newSize.width / size.width
        let verticalRatio = newSize.height / size.height
        
        let ratio = max(horizontalRatio, verticalRatio)
        let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
        UIGraphicsBeginImageContextWithOptions(newSize, true, 0)
        draw(in: CGRect(origin: CGPoint(x: 0, y: 0), size: newSize))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage!
    }
}
