import AppKit
import AVFoundation
import CoreImage
import CoreVideo
import Foundation

guard CommandLine.arguments.count == 3 else {
    fputs("Usage: swift make_loop.swift input.png output.mp4\n", stderr)
    exit(2)
}

let inputURL = URL(fileURLWithPath: CommandLine.arguments[1])
let outputURL = URL(fileURLWithPath: CommandLine.arguments[2])
let fileManager = FileManager.default
try? fileManager.removeItem(at: outputURL)

guard let source = CIImage(contentsOf: inputURL) else {
    fputs("Unable to read source image\n", stderr)
    exit(3)
}

let width = 1280
let height = 720
let frameRate: Int32 = 30
let frameCount = 180
let writer = try AVAssetWriter(outputURL: outputURL, fileType: .mp4)
let settings: [String: Any] = [
    AVVideoCodecKey: AVVideoCodecType.h264,
    AVVideoWidthKey: width,
    AVVideoHeightKey: height,
    AVVideoCompressionPropertiesKey: [
        AVVideoAverageBitRateKey: 2_200_000,
        AVVideoExpectedSourceFrameRateKey: frameRate,
        AVVideoMaxKeyFrameIntervalKey: frameRate * 2,
        AVVideoProfileLevelKey: AVVideoProfileLevelH264HighAutoLevel
    ]
]
let input = AVAssetWriterInput(mediaType: .video, outputSettings: settings)
input.expectsMediaDataInRealTime = false
let attributes: [String: Any] = [
    kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA,
    kCVPixelBufferWidthKey as String: width,
    kCVPixelBufferHeightKey as String: height,
    kCVPixelBufferIOSurfacePropertiesKey as String: [:]
]
let adaptor = AVAssetWriterInputPixelBufferAdaptor(assetWriterInput: input, sourcePixelBufferAttributes: attributes)
guard writer.canAdd(input) else { fatalError("Cannot add video input") }
writer.add(input)
guard writer.startWriting() else { fatalError(writer.error?.localizedDescription ?? "Cannot start writer") }
writer.startSession(atSourceTime: .zero)

let context = CIContext(options: [.cacheIntermediates: false])
let target = CGRect(x: 0, y: 0, width: width, height: height)
let baseScale = max(CGFloat(width) / source.extent.width, CGFloat(height) / source.extent.height)
let colorSpace = CGColorSpace(name: CGColorSpace.sRGB)!

for frame in 0..<frameCount {
    while !input.isReadyForMoreMediaData { usleep(1_000) }
    autoreleasepool {
        guard let pool = adaptor.pixelBufferPool else { fatalError("Missing pixel buffer pool") }
        var optionalBuffer: CVPixelBuffer?
        guard CVPixelBufferPoolCreatePixelBuffer(nil, pool, &optionalBuffer) == kCVReturnSuccess,
              let buffer = optionalBuffer else { fatalError("Cannot allocate pixel buffer") }

        let phase = CGFloat(frame) / CGFloat(frameCount - 1)
        let pulse = 1 + 0.027 * (1 - cos(phase * 2 * .pi)) / 2
        let scale = baseScale * pulse
        let scaled = source.transformed(by: CGAffineTransform(scaleX: scale, y: scale))
        let x = (CGFloat(width) - scaled.extent.width) / 2 - scaled.extent.minX
        let y = (CGFloat(height) - scaled.extent.height) / 2 - scaled.extent.minY
        let centered = scaled.transformed(by: CGAffineTransform(translationX: x, y: y))

        context.render(centered, to: buffer, bounds: target, colorSpace: colorSpace)
        let time = CMTime(value: CMTimeValue(frame), timescale: frameRate)
        guard adaptor.append(buffer, withPresentationTime: time) else {
            fatalError(writer.error?.localizedDescription ?? "Cannot append frame")
        }
    }
}

input.markAsFinished()
let semaphore = DispatchSemaphore(value: 0)
writer.finishWriting { semaphore.signal() }
semaphore.wait()
guard writer.status == .completed else {
    fatalError(writer.error?.localizedDescription ?? "Video export failed")
}
print(outputURL.path)
